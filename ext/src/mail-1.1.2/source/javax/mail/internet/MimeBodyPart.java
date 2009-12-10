/*
 * MimeBodyPart.java
 * Copyright (C) 2002, 2004, 2005 The Free Software Foundation
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.FileTypeMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import gnu.inet.util.GetSystemPropertyAction;
import gnu.mail.util.RFC2822OutputStream;

/**
 * A MIME body part.
 * Body parts are components of multipart parts.
 * <p>
 * The string representation of RFC822 and MIME header fields must contain
 * only US-ASCII characters. Non US-ASCII characters must be encoded as per
 * the rules in RFC 2047. This class does not enforce those rules; the
 * caller is expected to use <code>MimeUtility</code> to ensure that header
 * values are correctly encoded.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MimeBodyPart
  extends BodyPart
  implements MimePart
{

  /**
   * The data handler managing this part's content.
   */
  protected DataHandler dh;

  /**
   * The bytes of the content of this part, if the part can be stored in
   * memory.
   */
  protected byte[] content;

  /**
   * A SharedInputStream containing the bytes of this part, if it cannot be
   * stored in memory.
   */
  protected InputStream contentStream;

  /**
   * The headers of this body part.
   */
  protected InternetHeaders headers;

  /*
   * These constants are also referenced by MimeMessage.
   */
  static final String CONTENT_TYPE_NAME = "Content-Type";
  static final String CONTENT_DISPOSITION_NAME = "Content-Disposition";
  static final String CONTENT_TRANSFER_ENCODING_NAME =
    "Content-Transfer-Encoding";
  static final String CONTENT_ID_NAME = "Content-ID";
  static final String CONTENT_MD5_NAME = "Content-MD5";
  static final String CONTENT_LANGUAGE_NAME = "Content-Language";
  static final String CONTENT_DESCRIPTION_NAME = "Content-Description";
  
  static final String TEXT_PLAIN = "text/plain";

  /**
   * Constructor for an empty MIME body part.
   */
  public MimeBodyPart()
  {
    headers = new InternetHeaders();
  }

  /**
   * Constructor with an input stream.
   * The stream must be positioned at the start of a valid MIME body part
   * and terminate at the end of that body part: the boundary string must
   * not be included in the stream.
   * @param is the input stream
   */
  public MimeBodyPart(InputStream is)
    throws MessagingException
  {
    if (is instanceof SharedInputStream)
      {
        headers = new InternetHeaders(is);
        SharedInputStream sis = (SharedInputStream) is;
        contentStream = sis.newStream(sis.getPosition(), -1L);
        return;
      }
    
    // Buffer the stream if necessary
    if (!(is instanceof ByteArrayInputStream) &&
        !(is instanceof BufferedInputStream))
      {
        is = new BufferedInputStream(is);
      }
    
    // Read the headers
    headers = new InternetHeaders(is);
    
    // Read stream into byte array(see MimeMessage.parse())
    try
      {
        // TODO Make buffer size configurable
        int len = 1024;
        if (is instanceof ByteArrayInputStream)
          {
            len = is.available();
            content = new byte[len];
            is.read(content, 0, len);
          }
        else
          {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
            byte[] b = new byte[len];
            for (int l = is.read(b); l != -1; l = is.read(b))
              {
                bos.write(b, 0, l);
              }
            content = bos.toByteArray();
          }
      }
    catch (IOException e)
      {
        throw new MessagingException("I/O error", e);
      }
  }

  /**
   * Constructor with headers and byte content.
   * @param headers the header
   * @param content the byte content of this part
   */
  public MimeBodyPart(InternetHeaders headers, byte[] content)
    throws MessagingException
  {
    this.headers = headers;
    this.content = content;
  }

  /**
   * Returns the size of the content of this body part in bytes, or -1 if
   * the size cannot be determined.
   * <p>
   * Note that this number may not be an exact measure, but if not -1, it
   * will be suitable for display to the user.
   */
  public int getSize()
    throws MessagingException
  {
    if (content != null)
      {
        return content.length;
      }
    if (contentStream != null)
      {
        try
          {
            int len = contentStream.available();
            if (len > 0)
              {
                return len;
              }
          }
        catch (IOException e)
          {
          }
      }
    return -1;
  }

  /**
   * Returns the number of lines in the content of this body part, or -1 if
   * this number cannot be determined.
   * <p>
   * Note that this number may not be an exact measure, but if not -1, it
   * will be suitable for display to the user.
   */
  public int getLineCount()
    throws MessagingException
  {
    return -1;
  }

  /**
   * Returns the value of the RFC 822 Content-Type header field, or
   * "text/plain" if the header is not available.
   */
  public String getContentType()
    throws MessagingException
  {
    String contentType = getHeader(CONTENT_TYPE_NAME, null);
    if (contentType == null)
      {
        contentType = TEXT_PLAIN;
      }
    return contentType;
  }

  /**
   * Indicates whether this part is of the specified MIME type.
   * <p>
   * If the subtype of <code>mimeType</code> is the special character '*',
   * the subtype is ignored during the comparison.
   */
  public boolean isMimeType(String mimeType)
    throws MessagingException
  {
    String contentType = getContentType();
    try
      {
        return (new ContentType(contentType).match(mimeType));
      }
    catch (ParseException e)
      {
        return (getContentType().equalsIgnoreCase(mimeType));
      }
  }

  /**
   * Returns the value of the RFC 822 Content-Disposition header field, or
   * <code>null</code> if the header is not available.
   */
  public String getDisposition()
    throws MessagingException
  {
    String disposition = getHeader(CONTENT_DISPOSITION_NAME, null);
    if (disposition != null)
      {
        return new ContentDisposition(disposition).getDisposition();
      }
    return null;
  }

  /**
   * Sets the Content-Disposition header field of this part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setDisposition(String disposition)
    throws MessagingException
  {
    if (disposition == null)
      {
        removeHeader(CONTENT_DISPOSITION_NAME);
      }
    else
      {
        String value = getHeader(CONTENT_DISPOSITION_NAME, null);
        if (value != null)
          {
            ContentDisposition cd = new ContentDisposition(value);
            cd.setDisposition(disposition);
            disposition = cd.toString();
          }
        setHeader(CONTENT_DISPOSITION_NAME, disposition);
      }
  }

  /**
   * Returns the value of the Content-Transfer-Encoding header field.
   */
  public String getEncoding()
    throws MessagingException
  {
    String encoding = getHeader(CONTENT_TRANSFER_ENCODING_NAME, null);
    if (encoding != null)
      {
        encoding = encoding.trim();
        if (encoding.equalsIgnoreCase("7bit") || 
            encoding.equalsIgnoreCase("8bit") || 
            encoding.equalsIgnoreCase("quoted-printable") ||
            encoding.equalsIgnoreCase("base64"))
          {
            return encoding;
          }
        HeaderTokenizer ht =
          new HeaderTokenizer(encoding, HeaderTokenizer.MIME);
        for (boolean done = false; !done; )
          {
            HeaderTokenizer.Token token = ht.next();
            switch (token.getType())
              {
              case HeaderTokenizer.Token.EOF:
                done = true;
                break;
              case HeaderTokenizer.Token.ATOM:
                return token.getValue();
              }
          }
        return encoding;
      }
    return null;
  }

  /**
   * Returns the value of the Content-ID header field.
   */
  public String getContentID()
    throws MessagingException
  {
    return getHeader(CONTENT_ID_NAME, null);
  }

  /**
   * Sets the Content-ID header field of this part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   * @since JavaMail 1.3
   */
  public void setContentID(String cid)
    throws MessagingException
  {
    if (cid == null)
      {
        removeHeader(CONTENT_ID_NAME);
      }
    else
      {
        setHeader(CONTENT_ID_NAME, cid);
      }
  }

  /**
   * Returns the value of the Content-MD5 header field.
   */
  public String getContentMD5()
    throws MessagingException
  {
    return getHeader(CONTENT_MD5_NAME, null);
  }

  /**
   * Sets the Content-MD5 header field of this part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setContentMD5(String md5)
    throws MessagingException
  {
    setHeader(CONTENT_MD5_NAME, md5);
  }

  /**
   * Returns the languages specified in the Content-Language header of this
   * part, as defined by RFC 1766. This method returns <code>null</code> if 
   * this header is not available.
   */
  public String[] getContentLanguage()
    throws MessagingException
  {
    String header = getHeader(CONTENT_LANGUAGE_NAME, null);
    if (header != null)
      {
        HeaderTokenizer ht = new HeaderTokenizer(header, HeaderTokenizer.MIME);
        ArrayList acc = new ArrayList();
        for (boolean done = false; !done; )
          {
            HeaderTokenizer.Token token = ht.next();
            switch (token.getType())
              {
              case HeaderTokenizer.Token.EOF:
                done = true;
                break;
              case HeaderTokenizer.Token.ATOM:
                acc.add(token.getValue());
                break;
              }
          } 
        if (acc.size() > 0)
          {
            String[] languages = new String[acc.size()];
            acc.toArray(languages);
            return languages;
          }
      }
    return null;
  }

  /**
   * Sets the Content-Language header of this part.
   * @param languages the array of language tags
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setContentLanguage(String[] languages)
    throws MessagingException
  {
    if (languages != null && languages.length > 0)
      {
        StringBuffer buffer = new StringBuffer();
        buffer.append(languages[0]);
        for (int i = 1; i < languages.length; i++)
          {
            buffer.append(',');
            buffer.append(languages[i]);
          }
        setHeader(CONTENT_LANGUAGE_NAME, buffer.toString());
      }
    else
      {
        setHeader(CONTENT_LANGUAGE_NAME, null);
      }
  }

  /**
   * Returns the Content-Description header field of this part.
   * <p>
   * If the Content-Description field is encoded as per RFC 2047,
   * it is decoded and converted into Unicode.
   */
  public String getDescription()
    throws MessagingException
  {
    String header = getHeader(CONTENT_DESCRIPTION_NAME, null);
    if (header != null)
      {
        try
          {
            return MimeUtility.decodeText(header);
          }
        catch (UnsupportedEncodingException e)
          {
            return header;
          }
      }
    return null;
  }

  /**
   * Sets the Content-Description header field for this part.
   * <p>
   * If <code>description</code> contains non US-ASCII characters, it will
   * be encoded using the platform default charset.
   * @param description the content description
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setDescription(String description)
    throws MessagingException
  {
    setDescription(description, null);
  }

  /**
   * Sets the Content-Description header field for this part.
   * <p>
   * If <code>description</code> contains non US-ASCII characters, it will
   * be encoded using the specified charset.
   * @param description the content description
   * @param charset the charset used for encoding
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setDescription(String description, String charset)
    throws MessagingException
  {
    if (description != null)
      {
        try
          {
            setHeader(CONTENT_DESCRIPTION_NAME,
                       MimeUtility.encodeText(description, charset, null));
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException("Encode error", e);
          }
      }
    else
      {
        removeHeader(CONTENT_DESCRIPTION_NAME);
      }
  }

  /**
   * Returns the filename associated with this body part.
   * <p>
   * This method returns the value of the "filename" parameter from the
   * Content-Disposition header field.
   * If the latter is not available, it returns the value of the "name"
   * parameter from the Content-Type header field.
   */
  public String getFileName()
    throws MessagingException
  {
    String filename = null;
    String header = getHeader(CONTENT_DISPOSITION_NAME, null);
    if (header != null)
      {
        ContentDisposition cd = new ContentDisposition(header);
        filename = cd.getParameter("filename");
      }
    if (filename == null)
      {
        header = getHeader(CONTENT_TYPE_NAME, null);
        if (header != null)
          {
            try
              {
                ContentType contentType = new ContentType(header);
                filename = contentType.getParameter("name");
              }
            catch (ParseException e)
              {
              }
          }
      }
    PrivilegedAction a =
      new GetSystemPropertyAction("mail.mime.decodefilename");
    if ("true".equals(AccessController.doPrivileged(a)))
      {
        try
          {
            filename = MimeUtility.decodeText(filename);
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    return filename;
  }

  /**
   * Sets the filename associated with this body part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setFileName(String filename)
    throws MessagingException
  {
    PrivilegedAction a =
      new GetSystemPropertyAction("mail.mime.encodefilename");
    if ("true".equals(AccessController.doPrivileged(a)))
      {
        try
          {
            filename = MimeUtility.encodeText(filename);
          }
        catch (UnsupportedEncodingException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
    String header = getHeader(CONTENT_DISPOSITION_NAME, null);
    if (header == null)
      {
        header = "attachment";
      }
    ContentDisposition cd = new ContentDisposition(header);
    cd.setParameter("filename", filename);
    setHeader(CONTENT_DISPOSITION_NAME, cd.toString());

    // We will also set the "name" parameter of the Content-Type field
    // to preserve compatibility with nonconformant MUAs
    header = getHeader(CONTENT_TYPE_NAME, null);
    if (header == null)
      {
        DataHandler dh0 = getDataHandler();
        if (dh0 != null)
          header = dh0.getContentType();
        else
          header = "text/plain";
      }
    try
      {
        ContentType contentType = new ContentType(header);
        contentType.setParameter("name", filename);
        setHeader(CONTENT_TYPE_NAME, contentType.toString());
      }
    catch (ParseException e)
      {
      }
  }

  /**
   * Returns a decoded input stream for this part's content.
   * @exception IOException if an error occurs in the data handler layer
   */
  public InputStream getInputStream()
    throws IOException, MessagingException
  {
    return getDataHandler().getInputStream();
  }

  /**
   * Returns the unencoded bytes of the content. 
   */
  protected InputStream getContentStream()
    throws MessagingException
  {
    if (contentStream != null)
      {
        return ((SharedInputStream) contentStream).newStream(0L, -1L);
      }
    if (content != null)
      {
        return new ByteArrayInputStream(content);
      }
    throw new MessagingException("No content");
  }

  /**
   * Returns the unencoded bytes of the content without applying any
   * content transfer decoding.
   */
  public InputStream getRawInputStream()
    throws MessagingException
  {
    return getContentStream();
  }

  /**
   * Returns a data handler for accessing this part's content.
   */
  public DataHandler getDataHandler()
    throws MessagingException
  {
    if (dh == null)
      {
        dh = new DataHandler(new MimePartDataSource(this));
      }
    return dh;
  }

  /**
   * Returns this part's content as a Java object.
   * @exception IOException if an error occurred in the data handler layer
   */
  public Object getContent()
    throws IOException, MessagingException
  {
    return getDataHandler().getContent();
  }

  /**
   * Sets the content of this part using the specified data handler.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setDataHandler(DataHandler dh)
    throws MessagingException
  {
    this.dh = dh;
    // The Content-Type and Content-Transfer-Encoding headers may need to be
    // recalculated by the new DataHandler - see updateHeaders()
    removeHeader(CONTENT_TYPE_NAME);
    removeHeader(CONTENT_TRANSFER_ENCODING_NAME);
  }

  /**
   * Sets the content of this part using the specified Java object and MIME
   * type. Note that a data content handler for the MIME type must be
   * installed and accept objects of the type given.
   * @param o the content object
   * @param type the MIME type of the object
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setContent(Object o, String type)
    throws MessagingException
  {
    if (o instanceof Multipart)
      {
        setContent((Multipart) o);
      }
    else
      {
        setDataHandler(new DataHandler(o, type));
      }
  }

  /**
   * Sets the content of this part using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the text contains non US-ASCII characters, it will be encoded 
   * using the platform default charset.
   * @param text the text content
   */
  public void setText(String text)
    throws MessagingException
  {
    setText(text, null, "plain");
  }

  /**
   * Sets the content of this part using the specified text, and with a
   * MIME type of "text/plain".
   * <p>
   * If the text contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset used for any encoding
   */
  public void setText(String text, String charset)
    throws MessagingException
  {
    setText(text, charset, "plain");
  }
  
  /**
   * Sets the content of this part using the specified text, and with a
   * text MIME type of the specified subtype.
   * <p>
   * If the text contains non US-ASCII characters, it will be encoded 
   * using the specified charset.
   * @param text the text content
   * @param charset the charset used for any encoding
   * @param subtype the MIME text subtype (e.g. "plain", "html")
   * @since JavaMail 1.4
   */
  public void setText(String text, String charset, String subtype)
    throws MessagingException
  {
    if (charset == null)
      {
        // According to the API doc for getText(String), we may have to scan
        // the characters to determine the charset.
        // However this should work just as well and is hopefully relatively
        // cheap.
        charset =
          MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
      }
    if (subtype == null || "".equals(subtype))
      subtype = "plain";
    StringBuffer buffer = new StringBuffer();
    buffer.append("text/").append(subtype).append("; charset=");
    buffer.append(MimeUtility.quote(charset, HeaderTokenizer.MIME));
    setContent(text, buffer.toString());
  }

  /**
   * Sets the content of this part to be the specified multipart.
   * @param mp the multipart content
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setContent(Multipart mp)
    throws MessagingException
  {
    setDataHandler(new DataHandler(mp, mp.getContentType()));
    // Ensure component hierarchy
    mp.setParent(this);
  }

  /**
   * Writes this body part to the specified stream in RFC 822 format.
   * @exception IOException if an error occurs writing to the stream or in
   * the data handler layer
   */
  public void writeTo(OutputStream os)
    throws IOException, MessagingException
  {
    final String charset = "US-ASCII";
    final byte[] sep = { 0x0d, 0x0a };
    
    // Write the headers
    for (Enumeration e = getAllHeaderLines();
         e.hasMoreElements(); )
      {
        String line = (String) e.nextElement();
        StringTokenizer st = new StringTokenizer(line, "\r\n");
        int count = 0;
        while (st.hasMoreTokens())
          {
            String line2 = st.nextToken();
            if (count > 0 && line2.charAt(0) != '\t')
              {
                // Folded line must start with tab
                os.write(0x09);
              }
            /*
             * RFC 2822, section 2.1 states that each line should be no more
             * than 998 characters.
             * Ensure that any headers we emit have no lines longer than
             * this by folding the line.
             */
            int max = (count > 0) ? 997 : 998;
            while (line2.length() > max)
              {
                String left = line2.substring(0, max);
                byte[] bytes = left.getBytes(charset);
                os.write(bytes);
                os.write(sep);
                os.write(0x09);
                line2 = line2.substring(max);
                max = 997; // make space for the tab
              }
            byte[] bytes = line2.getBytes(charset);
            os.write(bytes);
            os.write(sep);
            count++;
          }
      }
    os.write(sep);
    os.flush();

    // Write the content
    os = MimeUtility.encode(os, getEncoding());
    getDataHandler().writeTo(os);
    os.flush();
  }

  /**
   * Returns all the values for the specified header name.
   * Note that headers may be encoded as per RFC 2047 if they contain
   * non-US-ASCII characters: these should be decoded.
   * @param name the header name
   */
  public String[] getHeader(String name)
    throws MessagingException
  {
    return headers.getHeader(name);
  }

  /**
   * Returns all the values for the specified header name as a single
   * string, with headers separated by the given delimiter.
   * If the delimiter is <code>null</code>, only the first header is
   * returned.
   * @param name the header name
   * @param delimiter the delimiter
   */
  public String getHeader(String name, String delimiter)
    throws MessagingException
  {
    return headers.getHeader(name, delimiter);
  }

  /**
   * Sets the specified header.
   * @param name the header name
   * @param value the header value
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void setHeader(String name, String value)
    throws MessagingException
  {
    headers.setHeader(name, value);
  }

  /**
   * Adds the specified header.
   * @param name the header name
   * @param value the header value
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void addHeader(String name, String value)
    throws MessagingException
  {
    headers.addHeader(name, value);
  }

  /**
   * Removes all headers with the specified name.
   * @param name the header name
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void removeHeader(String name)
    throws MessagingException
  {
    headers.removeHeader(name);
  }

  /**
   * Returns all the headers.
   * @return an Enumeration of Header objects
   */
  public Enumeration getAllHeaders()
    throws MessagingException
  {
    return headers.getAllHeaders();
  }

  /**
   * Returns all the headers with any of the given names.
   * @return an Enumeration of Header objects
   */
  public Enumeration getMatchingHeaders(String[] names)
    throws MessagingException
  {
    return headers.getMatchingHeaders(names);
  }

  /**
   * Returns all the headers without any of the given names.
   * @return an Enumeration of Header objects
   */
  public Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException
  {
    return headers.getNonMatchingHeaders(names);
  }

  /**
   * Adds an RFC 822 header-line to this part.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  public void addHeaderLine(String line)
    throws MessagingException
  {
    headers.addHeaderLine(line);
  }

  /**
   * Returns all the header-lines.
   * @return an Enumeration of Strings
   */
  public Enumeration getAllHeaderLines()
    throws MessagingException
  {
    return headers.getAllHeaderLines();
  }

  /**
   * Returns all the header-lines with any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    return headers.getMatchingHeaderLines(names);
  }

  /**
   * Returns all the header-lines without any of the given names.
   * @return an Enumeration of Strings
   */
  public Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    return headers.getNonMatchingHeaderLines(names);
  }

  /**
   * Updates the headers of this part, based on the content.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification
   * @exception IllegalStateException if this body part is obtained 
   * from a READ_ONLY folder
   */
  protected void updateHeaders()
    throws MessagingException
  {
    if (getDataHandler() != null)
      {
        try
          {
            String contentType = dh.getContentType();
            ContentType ct = new ContentType(contentType);
            if (ct.match("multipart/*"))
              {
                MimeMultipart mmp = (MimeMultipart) dh.getContent();
                mmp.updateHeaders();
              } 
            else if (ct.match("message/rfc822"))
              {
              }
            else
              {
                // Update Content-Transfer-Encoding
                if (getHeader(CONTENT_TRANSFER_ENCODING_NAME) == null)
                  {
                    setHeader(CONTENT_TRANSFER_ENCODING_NAME,
                               MimeUtility.getEncoding(dh));
                  }
              }
            
            // Update Content-Type if nonexistent,
            // and Content-Type "name" with Content-Disposition "filename"
            // parameter(see setFilename())
            if (getHeader(CONTENT_TYPE_NAME) == null)
              {
                String disposition = getHeader(CONTENT_DISPOSITION_NAME, null);
                if (disposition != null)
                  {
                    ContentDisposition cd =
                      new ContentDisposition(disposition);
                    String filename = cd.getParameter("filename");
                    if (filename != null)
                      {
                        ct.setParameter("name", filename);
                        contentType = ct.toString();
                      }
                  }
                setHeader(CONTENT_TYPE_NAME, contentType);
              }
          }
        catch (IOException e)
          {
            throw new MessagingException("I/O error", e);
          }
      }
  }

  /**
   * Use the specified file as the content for this part.
   * @param file the file
   * @since JavaMail 1.4
   */
  public void attachFile(File file)
    throws IOException, MessagingException
  {
    FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
    String contentType = map.getContentType(file);
    if (contentType == null)
      throw new MessagingException("Unable to determine MIME type of " + file);
    setContent(new FileInputStream(file), contentType);
    setFileName(file.getName());
  }

  /**
   * Use the specified file as the content for this part.
   * @param file the file
   * @since JavaMail 1.4
   */
  public void attachFile(String file)
    throws IOException, MessagingException
  {
    attachFile(new File(file));
  }
  
  /**
   * Saves the content of this part to the specified file.
   * @param file the file
   * @since JavaMail 1.4
   */
  public void saveFile(File file)
    throws IOException, MessagingException
  {
    OutputStream out = new FileOutputStream(file);
    try
      {
        out = MimeUtility.encode(out, getEncoding());
        getDataHandler().writeTo(out);
        out.flush();
      }
    finally
      {
        out.close();
      }
  }
  
  /**
   * Saves the content of this part to the specified file.
   * @param file the file
   * @since JavaMail 1.4
   */
  public void saveFile(String file)
    throws IOException, MessagingException
  {
    saveFile(new File(file));
  }
  
}

