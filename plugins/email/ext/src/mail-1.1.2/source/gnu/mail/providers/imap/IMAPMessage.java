/*
 * IMAPMessage.java
 * Copyright(C) 2003 Chris Burdess <dog@gnu.org>
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 * 
 * GNU JavaMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package gnu.mail.providers.imap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.ParameterList;
import javax.mail.internet.MimeMessage;

import gnu.inet.imap.IMAPConnection;
import gnu.inet.imap.IMAPConstants;
import gnu.inet.imap.MessageStatus;
import gnu.inet.imap.Pair;
import gnu.mail.providers.ReadOnlyMessage;

/**
 * The message class implementing the IMAP4 mail protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0
 */
public final class IMAPMessage
extends ReadOnlyMessage
{

  static final String FETCH_HEADERS = "BODY.PEEK[HEADER]";
  static final String FETCH_CONTENT = "BODY.PEEK[]";

  static final String PLUS_FLAGS = "+FLAGS";
  static final String MINUS_FLAGS = "-FLAGS";

  // BODYSTRUCTURE response atom indices
  static final int BS_CONTENT_TYPE = 0;
  static final int BS_CONTENT_SUBTYPE = 1;
  static final int BS_PARAMETERS = 2;
  static final int BS_ID = 3;
  static final int BS_DESCRIPTION = 4;
  static final int BS_ENCODING = 5;
  static final int BS_OCTETS = 6;
  static final int BS_LINES = 7;
  static final int BS_EXT_DISPOSITION = 8;
  static final int BS_EXT_LANGUAGE = 9;

  /**
   * If set, this contains the string value of the received date.
   */
  protected String internalDate = null;

  /**
   * The UID associated with this message.
   */
  protected long uid = -1L;

  /**
   * The date format used to parse IMAP INTERNALDATE values.
   */
  protected static final DateFormat internalDateFormat =
    new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss zzzzz");

  /**
   * If set, the current set of headers is complete.
   * If false, and a header is requested but returns null, all headers will
   * be requested from the server.
   */
  protected boolean headersComplete = false;

  /*
   * Parsed multipart object representing this message's content.
   */
  private IMAPMultipart multipart = null;

  IMAPMessage(IMAPFolder folder, InputStream in, int msgnum) 
    throws MessagingException 
  {
    super(folder, in, msgnum);
    flags = null;
  }

  IMAPMessage(IMAPFolder folder, int msgnum) 
    throws MessagingException 
  {
    super(folder, msgnum);
    flags = null;
  }

  /**
   * Fetches the flags fo this message.
   */
  void fetchFlags()
    throws MessagingException
  {
    String[] commands = new String[] { IMAPConstants.FLAGS };
    fetch(commands);
  }

  /**
   * Fetches the message header.
   */
  void fetchHeaders()
    throws MessagingException
  {
    String[] commands = new String[] { FETCH_HEADERS,
      IMAPConstants.INTERNALDATE };
    fetch(commands);
  }

  /**
   * Fetches the message body.
   */
  void fetchContent()
    throws MessagingException
  {
    String[] commands = new String[] { FETCH_CONTENT,
      IMAPConstants.INTERNALDATE };
    fetch(commands);
  }

  /**
   * Fetches the multipart corresponding to the message body.
   */
  void fetchMultipart()
    throws MessagingException
  {
    String[] commands = new String[] { IMAPConstants.BODYSTRUCTURE };
    fetch(commands);
  }

  /**
   * Fetches the UID.
   */
  void fetchUID()
    throws MessagingException
  {
    String[] commands = new String[] { IMAPConstants.UID };
    fetch(commands);
  }

  /**
   * Generic fetch routine.
   */
  void fetch(String[] commands)
    throws MessagingException
  {
    try
      {
        IMAPConnection connection =
         ((IMAPStore) folder.getStore()).getConnection();
        // Select folder
        if (!folder.isOpen())
          {
            folder.open(Folder.READ_WRITE);
          }
        int[] messages = new int[] { msgnum };
        synchronized (connection)
          {
            MessageStatus[] ms = connection.fetch(messages, commands);
            for (int i = 0; i < ms.length; i++)
              {
                if (ms[i].getMessageNumber() == msgnum)
                  {
                    update(ms[i]);
                  }
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Updates this message using the specified message status object.
   */
  void update(MessageStatus status)
    throws MessagingException
  {
    List code = status.getCode();
    int clen = code.size();
    for (int i = 0; i < clen; i += 2)
      {
        Object item = code.get(i);
        String key = null;
        List params = Collections.EMPTY_LIST;
        if (item instanceof Pair)
          {
            Pair pair = (Pair) item;
            key = pair.getKey();
            params = pair.getValue();
          }
        else if (item instanceof String)
          {
            key = (String) item;
          }
        else
          {
            throw new MessagingException("Unexpected status item: " + item);
          }
      
        if (key == IMAPConstants.BODY || key == IMAPConstants.RFC822)
          {
            byte[] literal = (byte[]) code.get(i + 1);
            int plen = params.size();
            if (plen == 0)
              {
                InputStream in = new ByteArrayInputStream(literal);
                parse(in);
              }
            else
              {
                for (int pi = 0; pi < plen; pi += 2)
                  {
                    Object pitem = params.get(pi);
                    String pkey = null;
                    if (pitem instanceof String)
                      {
                        pkey = (String) pitem;
                      }
                    else
                      {
                        throw new MessagingException("Unexpected status item: " +
                                                      pitem);
                      }
                    
                    if (pkey == IMAPConstants.HEADER)
                      {
                        InputStream in = new ByteArrayInputStream(literal);
                        headers = createInternetHeaders(in);
                        headersComplete = true;
                      }
                    else if (pkey == IMAPConstants.HEADER_FIELDS)
                      {
                        if (!headersComplete)
                          {
                            InputStream in = new ByteArrayInputStream(literal);
                            headers = createInternetHeaders(in);
                          }
                      }
                    else
                      {
                        throw new MessagingException("Unknown message status key: " +
                                                     pkey);
                      }
                  }
              }
          }
        else if (key == IMAPConstants.RFC822_HEADER)
          {
            byte[] literal = (byte[]) code.get(i + 1);
            InputStream in = new ByteArrayInputStream(literal);
            headers = createInternetHeaders(in);
            headersComplete = true;
          }
        else if (key == IMAPConstants.BODYSTRUCTURE)
          {
            List mlist = (List) code.get(i + 1);
            if (headers == null)
              {
                headers = new InternetHeaders();
              }
            multipart = parseMultipart(mlist, this, headers, null);
          }
        else if (key == IMAPConstants.ENVELOPE)
          {
            // TODO
          }
        else if (key == IMAPConstants.FLAGS)
          {
            List fl = (List) code.get(i + 1);
            flags = new IMAPFlags();
            for (Iterator j = fl.iterator(); j.hasNext(); )
              {
                Object f = j.next();
                if (f == IMAPConstants.FLAG_ANSWERED)
                  {
                    flags.add(Flags.Flag.ANSWERED);
                  }
                else if (f == IMAPConstants.FLAG_DELETED)
                  {
                    flags.add(Flags.Flag.DELETED);
                  }
                else if (f == IMAPConstants.FLAG_DRAFT)
                  {
                    flags.add(Flags.Flag.DRAFT);
                  }
                else if (f == IMAPConstants.FLAG_FLAGGED)
                  {
                    flags.add(Flags.Flag.FLAGGED);
                  }
                else if (f == IMAPConstants.FLAG_RECENT)
                  {
                    flags.add(Flags.Flag.RECENT);
                  }
                else if (f == IMAPConstants.FLAG_SEEN)
                  {
                    flags.add(Flags.Flag.SEEN);
                  }
                else if (f instanceof String)
                  {
                    flags.add((String) f);
                  }
              }
            ((IMAPFlags) flags).checkpoint();
          }
        else if (key == IMAPConstants.INTERNALDATE)
          {
            internalDate = (String) code.get(i + 1);
          }
        else if (key==IMAPConstants.UID)
          {
            uid = Long.parseLong((String) code.get(i + 1));
          }
        else
          {
            throw new MessagingException("Unknown message status key: " + key);
          }
      }
  }

  /*
   * Parse a multipart content object for the specified multipart Part.
   */
  IMAPMultipart parseMultipart(List list, Part parent,
                                InternetHeaders parentHeaders,
                                String baseSection)
    throws MessagingException
  {
    int len = list.size();
    if (len == 0)
      {
        throw new MessagingException("Empty [MIME-IMB] structure");
      }
    int offset = 0;
    // First parts, in lists
    Object value = list.get(offset);
    List partList = new ArrayList();
    List sectionList = new ArrayList();
    for (; value instanceof List; value = list.get(++offset))
      {
        String section = (baseSection == null) ?
          Integer.toString(offset+1) :
          baseSection + "." +(offset + 1);
        partList.add(value);
        sectionList.add(section);
      }
    // Next the multipart subtype
    String subtype = parseAtom(value).toLowerCase();
    IMAPMultipart m = new IMAPMultipart(this, parent, subtype);
    ContentType ct = new ContentType(m.getContentType());
    // Add the parts
    for (int i = 0; i < offset; i++)
      {
        List part = (List) partList.get(i);
        String section = (String) sectionList.get(i);
        m.addBodyPart(parseBodyPart(part, m, section));
      }
    // Now extension data
    //offset++;
    if (offset < len)
      {
        // Last 2 are disposition and language
        String disposition = parseAtom(list.get(len - 2));
        String language = parseAtom(list.get(len - 1));
        
        if (disposition != null)
          {
            parentHeaders.setHeader("Content-Disposition", disposition);
          }
        if (language != null)
          {
            parentHeaders.setHeader("Content-Language", language);
          }
        
        // Next any parameters
        // Note that there should only be 1 slot containing a list,
        // but servers sometimes return multiple lists
        List plist = new ArrayList();
        for (int i = offset; i < len - 2; i++)
          {
            value = list.get(i);
            if (value instanceof List)
              {
                plist.addAll((List) value);
              }
          }
        if (plist.size() > 0)
          {
            ParameterList params = parseParameterList(plist);
            ct = new ContentType(ct.getPrimaryType(), subtype, params);
          }
      }
    parentHeaders.setHeader("Content-Type", ct.toString());
    return m;
  }

  /*
   * Parse a body part for the specified multipart content object.
   */
  IMAPBodyPart parseBodyPart(List list, IMAPMultipart parent, String section)
    throws MessagingException
  {
    int len = list.size();
    if (len == 0)
      {
        throw new MessagingException("Empty [MIME-IMB] structure");
      }
    Object arg1 = list.get(0);
    if (arg1 instanceof List)
      {
        // Multipart body part
        InternetHeaders h = new InternetHeaders();
        IMAPBodyPart part = new IMAPBodyPart(this, parent, section, h, -1, -1);
        IMAPMultipart m = parseMultipart(list, part, h, section);
        part.multipart = m;
        return part;
      }
    
    if (len < 8)
      {
        throw new MessagingException("Unexpected number of fields in " +
                                      "[MIME-IMB] structure: " + list);
      }
        
    // Basic fields
    String type = parseAtom(list.get(BS_CONTENT_TYPE)).toLowerCase();
    String subtype = parseAtom(list.get(BS_CONTENT_SUBTYPE)).toLowerCase();
    ParameterList params = parseParameterList(list.get(BS_PARAMETERS));
    String id = parseAtom(list.get(BS_ID));
    String description = parseAtom(list.get(BS_DESCRIPTION));
    String encoding = parseAtom(list.get(BS_ENCODING));
    String sizeVal = parseAtom(list.get(BS_OCTETS));
    String linesVal = parseAtom(list.get(BS_LINES));

    int size = -1;
    int lines = -1;
    try
      {
        if (sizeVal != null)
          {
            size = Integer.parseInt(sizeVal);
          }
        if (linesVal != null)
          {
            lines = Integer.parseInt(linesVal);
          }
      }
    catch (NumberFormatException e)
      {
        throw new MessagingException("Expecting number in [MIME-IMB] " +
                                      "structure: " + list);
      }
    
    ContentType ct = new ContentType(type, subtype, params);
    InternetHeaders h = new InternetHeaders();
    h.setHeader("Content-Type", ct.toString());
    if (id != null)
      {
        h.setHeader("Content-Id", id);
      }
    if (description != null)
      {
        h.setHeader("Content-Description", description);
      }
    if (encoding != null)
      {
        h.setHeader("Content-Transfer-Encoding", encoding);
      }
    
    // Extension fields
    if (len > 8)
      {
        Object dispositionVal = list.get(BS_EXT_DISPOSITION);
        String disposition = parseAtom(dispositionVal);
        if (disposition != null)
          {
            h.setHeader("Content-Disposition", disposition);
          }
        else if (dispositionVal instanceof List)
          {
            List d = (List) dispositionVal;
            if (d != null && d.size() == 2)
              {
                disposition = parseAtom(d.get(0));
                ParameterList pl = parseParameterList(d.get(1));
                h.setHeader("Content-Disposition", disposition + pl.toString());
              }
          }
      }
    if (len > 9)
      {
        String language = parseAtom(list.get(BS_EXT_LANGUAGE));
        if (language != null)
          {
            h.setHeader("Content-Language", language);
          }
      }

    return new IMAPBodyPart(this, parent, section, h, size, lines);
  }

  String parseAtom(Object value)
  {
    if (value instanceof String && !(value.equals(IMAPConstants.NIL)))
      {
        return (String) value;
      }
    return null;
  }

  ParameterList parseParameterList(Object params)
  {
    if (params instanceof List)
      {
        List list = (List) params;
        int len = list.size();
        ParameterList plist = new ParameterList();
        for (int i = 0; i < len - 1; i += 2)
          {
            Object key = list.get(i);
            Object value = list.get(i + 1);
            if (key instanceof String && value instanceof String)
              {
                String atom = parseAtom(value);
                if (atom != null)
                  plist.set((String) key, atom);
              }
          }
        return plist;
      }
    return null;
  }

  /**
   * Returns the date on which this message was received.
   */
  public Date getReceivedDate()
    throws MessagingException
  {
    if (internalDate == null && headers == null)
      {
        fetchHeaders(); // seems reasonable
      }
    if (internalDate == null)
      {
        return null;
      }
    try
      {
        return internalDateFormat.parse(internalDate);
      }
    catch (ParseException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  // -- Content access --

  /**
   * Returns a data handler for this message's content.
   */
  public DataHandler getDataHandler() 
    throws MessagingException
  {
    // Hook into BODYSTRUCTURE method
    ContentType ct = new ContentType(getContentType());
    // TODO message/* content-types
    if ("multipart".equalsIgnoreCase(ct.getPrimaryType()))
      {
        if (multipart == null)
          {
            fetchMultipart();
          }
        return new DataHandler(new IMAPMultipartDataSource(multipart));
      }
    if (content == null)
      {
        fetchContent();
      }
    return super.getDataHandler();
  }

  public Object getContent()
    throws MessagingException, IOException
  {
    ContentType ct = new ContentType(getContentType());
    if ("multipart".equalsIgnoreCase(ct.getPrimaryType()))
      {
        if (multipart == null)
          {
            fetchMultipart();
          }
        return multipart;
      }
    return super.getContent();
  }

  /**
   * Returns the raw content stream.
   */
  protected InputStream getContentStream() 
    throws MessagingException 
  {
    if (content == null)
      {
        fetchContent();
      }
    return super.getContentStream();
  }

  // -- Header access --
  
  /**
   * Returns the specified header field.
   */
  public String[] getHeader(String name) 
    throws MessagingException 
  {
    if (headers == null)
      {
        fetchHeaders();
      }
    String[] header = super.getHeader(name);
    if (header == null && !headersComplete)
      {
        fetchHeaders();
      }
    header = super.getHeader(name);
    return header;
  }

  /**
   * Returns the specified header field.
   */
  public String getHeader(String name, String delimiter) 
    throws MessagingException 
  {
    if (headers == null)
      {
        fetchHeaders();
      }
    String header = super.getHeader(name, delimiter);
    if (header == null && !headersComplete)
      {
        fetchHeaders();
      }
    header = super.getHeader(name, delimiter);
    return header;
  }

  public Enumeration getAllHeaders() 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getAllHeaders();
  }

  public Enumeration getAllHeaderLines() 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getAllHeaderLines();
  }

  public Enumeration getMatchingHeaders(String[] names) 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getMatchingHeaders(names);
  }

  public Enumeration getMatchingHeaderLines(String[] names) 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getMatchingHeaderLines(names);
  }

  public Enumeration getNonMatchingHeaders(String[] names) 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getNonMatchingHeaders(names);
  }

  public Enumeration getNonMatchingHeaderLines(String[] names) 
    throws MessagingException 
  {
    if (!headersComplete)
      {
        fetchHeaders();
      }
    return super.getNonMatchingHeaderLines(names);
  }

  // -- Flags access --

  public Flags getFlags()
    throws MessagingException
  {
    if (flags == null)
      {
        fetchFlags();
      }
    return super.getFlags();
  }

  public boolean isSet(Flags.Flag flag)
    throws MessagingException
  {
    if (flags == null)
      {
        fetchFlags();
      }
    return super.isSet(flag);
  }

  /**
   * Set the specified flags.
   */
  public void setFlags(Flags flag, boolean set)
    throws MessagingException
  {
    if (flags == null)
      {
        fetchFlags();
      }
    try
      {
        if (set)
          {
            flags.add(flag);
          }
        else
          {
            flags.remove(flag);
          }
        
        // Create lists of flags to send to the server
        IMAPFlags iflags = (IMAPFlags) flags;
        List aflagList = iflags.getAddedFlags();
        String[] aflags = new String[aflagList.size()];
        aflagList.toArray(aflags);
        List rflagList = iflags.getRemovedFlags();
        String[] rflags = new String[rflagList.size()];
        rflagList.toArray(rflags);
        
        // Perform store
        if (aflags.length > 0 || rflags.length > 0)
          {
            IMAPStore store = (IMAPStore) folder.getStore();
            IMAPConnection c = store.getConnection();
            int[] messages = new int[] { msgnum };
            if (aflags.length > 0)
              {
                c.store(messages, PLUS_FLAGS, aflags);
              }
            if (rflags.length > 0)
              {
                c.store(messages, MINUS_FLAGS, rflags);
              }
            flags = null; // Reread from server next time
          }
      }
    catch (IOException e)
      {
        flags = null; // will be re-read next time
        throw new MessagingException(e.getMessage(), e);
      }
  }
  
  // -- Utility --

  public void writeTo(OutputStream msgStream) 
    throws IOException, MessagingException 
  {
    if (headers == null)
      {
        fetchHeaders();
      }
    if (content == null)
      {
        fetchContent();
      }
    super.writeTo(msgStream);
  }

  public void writeTo(OutputStream msgStream, String[] ignoreList) 
    throws IOException, MessagingException 
  {
    if (headers == null)
      {
        fetchHeaders();
      }
    if (content == null)
      {
        fetchContent();
      }
    super.writeTo(msgStream, ignoreList);
  }

}
