/*
 * MimePartDataSource.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import javax.activation.DataSource;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

/**
 * A data source that manages content for a MIME part.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MimePartDataSource
  implements DataSource, MessageAware
{

  /**
   * The part.
   * @since JavaMail 1.4
   */
  protected MimePart part;

  /*
   * Manages a MessageContext on behalf of the part.
   * @see #getMessageContext
   */
  private MessageContext context;

  /**
   * Constructor with a MIME part.
   */
  public MimePartDataSource(MimePart part)
  {
    this.part = part;
  }

  /**
   * Returns an input stream from the MIME part.
   * <p>
   * This method applies the appropriate transfer-decoding, based on the
   * Content-Transfer-Encoding header of the MimePart.
   */
  public InputStream getInputStream()
    throws IOException
  {
    try
      {
        InputStream is;
        if (part instanceof MimeBodyPart)
          {
            is = ((MimeBodyPart) part).getContentStream();
          }
        else if (part instanceof MimeMessage)
          {
            is = ((MimeMessage) part).getContentStream();
          }
        else
          {
            throw new MessagingException("Unknown part type");
          }
        
        String encoding = part.getEncoding();
        return (encoding != null) ? MimeUtility.decode(is, encoding) : is;
      }
    catch (MessagingException e)
      {
        throw new IOException(e.getMessage());
      }
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    throw new UnknownServiceException();
  }

  public String getContentType()
  {
    try
      {
        return part.getContentType();
      }
    catch (MessagingException e)
      {
        return null;
      }
  }

  public String getName()
  {
    // Shouldn't this return the filename parameter of the
    // Content-Disposition of a MimeBodyPart, if available?
    return "";
  }

  /**
   * Returns the message context for the current part.
   */
  public MessageContext getMessageContext()
  {
    if (context == null)
      {
        context = new MessageContext(part);
      }
    return context;
  }
  
}

