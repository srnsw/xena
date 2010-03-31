/*
 * MimeMultipart.java
 * Copyright (C) 2002, 2005 The Free Software Foundation
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

package javax.mail.internet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

import gnu.inet.util.CRLFOutputStream;
import gnu.inet.util.GetSystemPropertyAction;
import gnu.inet.util.LineInputStream;

/**
 * A MIME multipart container.
 * <p>
 * The default multipart subtype is "mixed". However, an application can
 * construct a MIME multipart object of any subtype using the
 * <code>MimeMultipart(String)</code> constructor.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MimeMultipart
  extends Multipart
{

  /**
   * The data source supplying the multipart data.
   */
  protected DataSource ds;

  /**
   * Indicates whether the data from the input stream has been parsed yet.
   */
  protected boolean parsed;

  /**
   * Indicates whether the final boundary line of the multipart has been
   * seen.
   */
  private boolean complete;

  /**
   * The preamble text before the first boundary line.
   */
  private String preamble;

  /**
   * Constructor for an empty MIME multipart of type "multipart/mixed".
   */
  public MimeMultipart()
  {
    this("mixed");
  }

  /**
   * Constructor for an empty MIME multipart of the given subtype.
   */
  public MimeMultipart(String subtype)
  {
    String boundary = MimeUtility.getUniqueBoundaryValue();
    ContentType ct = new ContentType("multipart", subtype, null);
    ct.setParameter("boundary", boundary);
    contentType = ct.toString();
    parsed = true;
  }

  /**
   * Constructor with a given data source.
   * @param ds the data source, which can be a MultipartDataSource
   */
  public MimeMultipart(DataSource ds)
    throws MessagingException
  {
    if (ds instanceof MessageAware)
      {
        MessageContext mc = ((MessageAware) ds).getMessageContext();
        setParent(mc.getPart());
      }
    if (ds instanceof MultipartDataSource)
      {
        setMultipartDataSource((MultipartDataSource) ds);
        parsed = true;
      }
    else
      {
        this.ds = ds;
        contentType = ds.getContentType();
        parsed = false;
      }
  }

  /**
   * Sets the subtype.
   */
  public void setSubType(String subtype)
    throws MessagingException
  {
    ContentType ct = new ContentType(contentType);
    ct.setSubType(subtype);
    contentType = ct.toString();
  }

  /**
   * Returns the number of component body parts.
   */
  public int getCount()
    throws MessagingException
  {
    synchronized (this)
      {
        parse();
        return super.getCount();
      }
  }

  /**
   * Returns the specified body part.
   * Body parts are numbered starting at 0.
   * @param index the body part index
   * @exception MessagingException if no such part exists
   */
  public BodyPart getBodyPart(int index)
    throws MessagingException
  {
    synchronized (this)
      {
        parse();
        return super.getBodyPart(index);
      }
  }

  /**
   * Returns the body part identified by the given Content-ID (CID).
   * @param CID the Content-ID of the desired part
   */
  public BodyPart getBodyPart(String CID)
    throws MessagingException
  {
    synchronized (this)
      {
        parse();
        int count = getCount();
        for (int i = 0; i < count; i++)
          {
            MimeBodyPart bp = (MimeBodyPart) getBodyPart(i);
            String contentID = bp.getContentID();
            if (contentID != null && contentID.equals(CID))
              {
                return bp;
              }
          }
        return null;
      }
  }

  /**
   * Updates the headers of this part to be consistent with its content.
   */
  protected void updateHeaders()
    throws MessagingException
  {
    if (parts == null)
      {
        return;
      }
    synchronized (parts)
    {
      int len = parts.size();
      for (int i = 0; i < len; i++)
        {
         ((MimeBodyPart) parts.get(i)).updateHeaders();
        }
    }
  }

  /**
   * Writes this multipart to the specified output stream.
   * This method iterates through all the component parts, outputting each
   * part separated by the Content-Type boundary parameter.
   */
  public void writeTo(OutputStream os)
    throws IOException, MessagingException
  {
    final String charset = "US-ASCII";
    final byte[] sep = { 0x0d, 0x0a };
    
    parse();
    ContentType ct = new ContentType(contentType);
    String boundaryParam = ct.getParameter("boundary");
    if (boundaryParam == null)
      {
        PrivilegedAction a =
          new GetSystemPropertyAction("mail.mime.multipart.ignore"+
                                      "missingboundaryparameter");
        if ("false".equals(AccessController.doPrivileged(a)))
          throw new MessagingException("Missing boundary parameter");
      }
    byte[] boundary = ("--" + boundaryParam).getBytes(charset);
    
    if (preamble != null)
      os.write(preamble.getBytes(charset));
    
    synchronized (parts)
      {
        int len = parts.size();
        for (int i = 0; i < len; i++)
          {
            os.write(boundary);
            os.write(sep);
            os.flush();
            ((MimeBodyPart) parts.get(i)).writeTo(os);
            os.write(sep);
          }
      }

    boundary = ("--" + boundaryParam + "--").getBytes(charset);
    os.write(boundary);
    os.write(sep);
    os.flush();
  }

  /**
   * Parses the body parts from this multipart's data source.
   */
  protected void parse()
    throws MessagingException
  {
    if (parsed)
      {
        return;
      }
    synchronized (this)
      {
        InputStream is = null;
        SharedInputStream sis = null;
        try
          {
            is = ds.getInputStream();
            if (is instanceof SharedInputStream)
              {
                sis = (SharedInputStream) is;
              }
            // buffer it
            if (!(is instanceof ByteArrayInputStream) && 
                !(is instanceof BufferedInputStream))
              {
                is = new BufferedInputStream(is);
              }
            ContentType ct = new ContentType(contentType);
            String boundaryParam = ct.getParameter("boundary");
            if (boundaryParam == null)
              {
                PrivilegedAction a =
                  new GetSystemPropertyAction("mail.mime.multipart.ignore"+
                                              "missingboundaryparameter");
                if ("false".equals(AccessController.doPrivileged(a)))
                  throw new MessagingException("Missing boundary parameter");
              }
            String boundary = (boundaryParam == null) ? null :
              "--" + boundaryParam;
            
            LineInputStream lis = new LineInputStream(is);
            String line;
            StringBuffer preambleBuf = null;
            while ((line = lis.readLine()) != null)
              {
                String l = trim(line);
                if (boundary == null && l.startsWith("--") &&
                    !l.endsWith("--"))
                  {
                    boundary = l.substring(2).trim();
                    break;
                  }
                else if (l.equals(boundary))
                  {
                    break;
                  }
                if (preambleBuf == null)
                  preambleBuf = new StringBuffer();
                preambleBuf.append(line);
                preambleBuf.append('\n');
              }
            if (preambleBuf != null)
              preamble = preambleBuf.toString();
            if (line == null)
              {
                throw new MessagingException("No start boundary");
              }
            
            byte[] bbytes = boundary.getBytes();
            int blen = bbytes.length;
            
            long start = 0L, end = 0L;
            for (boolean done = false; !done;)
              {
                InternetHeaders headers = null;
                if (sis != null)
                  {
                    start = sis.getPosition();
                    do
                      {
                        line = trim(lis.readLine());
                      }
                    while (line != null && line.length() > 0);
                    if (line == null)
                      {
                        throw new IOException("EOF before content body");
                      }
                  }
                else
                  {
                    headers = createInternetHeaders(is);
                  }
                ByteArrayOutputStream bos = null;
                if (sis == null)
                  {
                    bos = new ByteArrayOutputStream();
                  }
                
                // NB this routine uses the InputStream.mark() method
                // if it is not supported by the underlying stream
                // we will run into problems
                if (!is.markSupported())
                  {
                    String cn = is.getClass().getName();
                    throw new MessagingException("FIXME: mark not supported" +
                                                  " on underlying input stre" +
                                                  "am: " + cn);
                  }
                boolean eol = true;
                int last = -1;
                int afterLast = -1;
                while (true)
                  {
                    int c;
                    if (eol)
                      {
                        is.mark(blen + 1024);
                        int pos = 0;
                        while (pos < blen)
                          {
                            if (is.read() != bbytes[pos])
                              {
                                break;
                              }
                            pos++;
                          }
                        
                        if (pos == blen)
                          {
                            c = is.read();
                            if (c == '-' && is.read() == '-')
                              {
                                done = true;
                                complete = true;
                                break;
                              }
                            while (c == ' ' || c == '\t')
                              {
                                c = is.read();
                              }
                            if (c == '\r')
                              {
                                is.mark(1);
                                if (is.read() != '\n')
                                  {
                                    is.reset();
                                  }
                                break;
                              }
                            if (c == '\n')
                              {
                                break;
                              }
                          }
                        if (bos != null && last != -1)
                          {
                            bos.write(last);
                            if (afterLast != -1)
                              {
                                bos.write(afterLast);
                              }
                            last = afterLast = -1;
                          }
                        is.reset();
                      }
                    c = is.read();
                    if (c < 0)
                      {
                        done = true;
                        break;
                      }
                    else if (c == '\r' || c == '\n')
                      {
                        eol = true;
                        if (sis != null)
                          {
                            end = sis.getPosition() - 1L;
                          }
                        last = c;
                        if (c == '\r')
                          {
                            is.mark(1);
                            if ((c = is.read()) == '\n')
                              {
                                afterLast = c;
                              }
                            else
                              {
                                is.reset();
                              }
                          }
                      }
                    else
                      {
                        eol = false;
                        if (bos != null)
                          {
                            bos.write(c);
                          }
                      }
                  }
                
                // Create a body part from the stream
                MimeBodyPart bp;
                if (sis != null)
                  {
                    bp = createMimeBodyPart(sis.newStream(start, end));
                  }
                else
                  {
                    bp = createMimeBodyPart(headers, bos.toByteArray());
                  }
                addBodyPart(bp);
              }
            
          }
        catch (IOException e)
          {
            throw new MessagingException("I/O error", e);
          }
        parsed = true;
        if (!complete)
          {
            PrivilegedAction a =
              new GetSystemPropertyAction("mail.mime.multipart.ignoremissingendboundary");
            if ("false".equals(AccessController.doPrivileged(a)))
              throw new MessagingException("Missing end boundary");
          }
      }
  }

  /**
   * Indicates whether the final boundary line for this multipart has been
   * parsed.
   * @since JavaMail 1.4
   */
  public boolean isComplete()
    throws MessagingException
  {
    return complete;
  }

  /**
   * Returns the preamble text (if any) before the first boundary line in
   * this multipart's body.
   * @since JavaMail 1.4
   */
  public String getPreamble()
    throws MessagingException
  {
    return preamble;
  }

  /**
   * Sets the preamble text to be emitted before the first boundary line.
   * @param preamble the preamble text
   * @since JavaMail 1.4
   */
  public void setPreamble(String preamble)
    throws MessagingException
  {
    this.preamble = preamble;
  }

  /*
   * Ensures that CR is stripped from the end of the given line.
   */
  private static String trim(String line)
  {
    if (line == null)
      {
        return null;
      }
    line = line.trim();
    int len = line.length();
    if (len > 0 && line.charAt(len - 1) == '\r')
      {
        line = line.substring(0, len - 1);
      }
    return line;
  }
  
  /**
   * Creates headers from the specified input stream.
   * @param is the input stream to read the headers from
   */
  protected InternetHeaders createInternetHeaders(InputStream is)
    throws MessagingException
  {
    return new InternetHeaders(is);
  }
  
  /**
   * Creates a MIME body part object from the given headers and byte content.
   * @param headers the part headers
   * @param content the part content
   */
  protected MimeBodyPart createMimeBodyPart(InternetHeaders headers,
                                            byte[] content)
    throws MessagingException
  {
    return new MimeBodyPart(headers, content);
  }
  
  /**
   * Creates a MIME body part from the specified input stream.
   * @param is the input stream to parse the part from
   */
  protected MimeBodyPart createMimeBodyPart(InputStream is)
    throws MessagingException
  {
    return new MimeBodyPart(is);
  }
  
}

