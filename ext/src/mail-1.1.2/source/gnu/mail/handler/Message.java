/*
 * Message.java
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
import java.util.Properties;
import javax.activation.*;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * A JAF data content handler for the message/* family of MIME content
 * types.
 */
public abstract class Message
  implements DataContentHandler
{

  /**
   * Our favorite data flavor.
   */
  protected DataFlavor flavor;
  
  /**
   * Constructor specifying the data flavor.
   * @param mimeType the MIME content type
   * @param description the description of the content type
   */
  protected Message(String mimeType, String description)
  {
    flavor = new ActivationDataFlavor(javax.mail.Message.class, mimeType,
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
   * @return a message
   */
  public Object getContent(DataSource source)
    throws IOException
  {
    try
    {
      Session session = null;
      if (source instanceof MessageAware)
      {
        MessageAware ma = (MessageAware)source;
        MessageContext context = ma.getMessageContext();
        session = context.getSession();
      }
      else
      {
        Properties props = null;
        /* Do we want to pass the system properties in? */
        session = Session.getDefaultInstance(props, null);
      }
      InputStream in = source.getInputStream();
      return new MimeMessage(session, in);
    }
    catch (MessagingException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
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
    if (object instanceof javax.mail.Message)
    {
      try
      {
       ((javax.mail.Message)object).writeTo(out);
      }
      catch (MessagingException e)
      {
        /* not brilliant as we lose any associated exception */
        throw new IOException(e.getMessage());
      }
    }
    else
      throw new UnsupportedDataTypeException();
  }

}
