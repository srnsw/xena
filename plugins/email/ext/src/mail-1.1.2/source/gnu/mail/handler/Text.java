/*
 * Text.java
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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

package gnu.mail.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import javax.activation.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

/**
 * A JAF data content handler for the text/* family of MIME content types.
 * This provides the basic behaviour for any number of text-handling
 * subtypes which simply need to override their default constructor to provide
 * the correct MIME content-type and description.
 */
public abstract class Text
  implements DataContentHandler
{

  /**
   * Our favorite data flavor.
   * Yum yum.
   */
  protected DataFlavor flavor;
  
  /**
   * Constructor specifying the data flavor.
   * @param mimeType the MIME content type
   * @param description the description of the content type
   */
  protected Text(String mimeType, String description)
  {
    flavor = new ActivationDataFlavor(java.lang.String.class, mimeType,
        description);
  }

  /**
   * Returns an array of DataFlavor objects indicating the flavors the data
   * can be provided in.
   * @return the DataFlavors
   */
  public DataFlavor[] getTransferDataFlavors()
  {
    DataFlavor[] flavors = new DataFlavor[1];
    flavors[0] = flavor;
    return flavors;
  }

  /**
   * Returns an object which represents the data to be transferred.
   * The class of the object returned is defined by the representation class
   * of the flavor.
   * @param flavor the data flavor representing the requested type
   * @param source the data source representing the data to be converted
   * @return the constructed object
   */
  public Object getTransferData(DataFlavor flavor, DataSource source)
    throws UnsupportedFlavorException, IOException
  {
    if (this.flavor.equals(flavor))
      return getContent(source);
    return null;
  }

  /**
   * Return an object representing the data in its most preferred form.
   * Generally this will be the form described by the first data flavor
   * returned by the <code>getTransferDataFlavors</code> method.
   * @param source the data source representing the data to be converted
   * @return the constructed object
   */
  public Object getContent(DataSource source)
    throws IOException
  {
    InputStream in = source.getInputStream();
    String mimeType = source.getContentType();
    String charset = getJavaCharset(mimeType);
    InputStreamReader reader = new InputStreamReader(in, charset);
    char[] buf = new char[4096]; // TODO make configurable
    StringBuffer buffer = new StringBuffer();
    while (true)
    {
      int len = reader.read(buf);
      if (len > -1)
        buffer.append(new String(buf, 0, len));
      else
        break;
    }
    return buffer.toString();
  }

  /**
   * Convert the object to a byte stream of the specified MIME type and
   * write it to the output stream.
   * @param object the object to be converted
   * @param mimeType the requested MIME content type to write as
   * @param out the output stream into which to write the converted object
   */
  public void writeTo(Object object, String mimeType, OutputStream out)
    throws IOException
  {
    // We only handle strings or arrays of byte or char
    String text = null;
    if (object instanceof String)
      text = (String)object;
    else if (object instanceof byte[])
      text = new String((byte[])object);
    else if (object instanceof char[])
      text = new String((char[])object);
    else // Last attempt to get something useful
      text = object.toString();
    String charset = getJavaCharset(mimeType);
    OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    writer.write(text);
    writer.flush();
  }

  /**
   * Returns the Java character set corresponding to the MIME charset
   * parameter of the content type value.
   * The default value is the Java character set mapped to the MIME charset
   * "us-ascii".
   * @return a java charset name
   */
  protected static String getJavaCharset(String contentType)
  {
    String charset = "us-ascii";
    if (contentType!=null)
    {
      try
      {
        ContentType ct = new ContentType(contentType);
        String ctCharset = ct.getParameter("charset");
        if (ctCharset!=null)
          charset = ctCharset;
      }
      catch (ParseException e)
      {
      }
    }
    return MimeUtility.javaCharset(charset);
  }
  
}
