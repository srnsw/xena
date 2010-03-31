/*
 * ByteArrayDataSource.java
 * Copyright (C) 2005 The Free Software Foundation
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

package javax.mail.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

/**
 * Data source backed by a byte array.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 * @since JavaMail 1.4
 */
public class ByteArrayDataSource
  implements DataSource
{

  private byte[] data;

  private String type;

  private String name = "";

  /**
   * Constructor with a byte array.
   * @param data the byte array
   * @param type the MIME type
   */
  public ByteArrayDataSource(byte[] data, String type)
  {
    this.data = data;
    this.type = type;
  }

  /**
   * Constructor with an input stream.
   * @param is the input stream (will be read to end but not closed)
   * @param type the MIME type
   */
  public ByteArrayDataSource(InputStream is, String type)
    throws IOException
  {
    ByteArrayOutputStream sink = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    for (int len = is.read(buf); len != -1; len = is.read(buf))
      sink.write(buf, 0, len);
    data = sink.toByteArray();
    this.type = type;
  }

  /**
   * Constructor with a String.
   * The MIME type should include a charset parameter specifying the charset
   * to use to encode the string; otherwise, the platform default is used.
   * @param data the string
   * @param type the MIME type
   */
  public ByteArrayDataSource(String data, String type)
    throws IOException
  {
    try
      {
        ContentType ct = new ContentType(type);
        String charset = ct.getParameter("charset");
        String jcharset = (charset == null) ?
          MimeUtility.getDefaultJavaCharset() :
          MimeUtility.javaCharset(charset);
        if (jcharset == null)
          throw new UnsupportedEncodingException(charset);
        this.data = data.getBytes(jcharset);
        this.type = type;
      }
    catch (ParseException e)
      {
        IOException e2 = new IOException("can't parse MIME type");
        e2.initCause(e);
        throw e2;
      }
  }

  /**
   * Returns an input stream for the data.
   */
  public InputStream getInputStream()
    throws IOException
  {
    return new ByteArrayInputStream(data);
  }

  /**
   * Returns an output stream.
   * The output stream throws an exception if written to.
   */
  public OutputStream getOutputStream()
    throws IOException
  {
    return new ErrorOutputStream();
  }

  /**
   * Returns the MIME type of the data.
   */
  public String getContentType()
  {
    return type;
  }

  /**
   * Returns the name of the data.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name of the data.
   * @param name the new name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  static class ErrorOutputStream
    extends OutputStream
  {

    public void write(int c)
      throws IOException
    {
      throw new IOException("writing to this stream is not allowed");
    }
    
    public void write(byte[] b, int off, int len)
      throws IOException
    {
      throw new IOException("writing to this stream is not allowed");
    }
    
  }
}
