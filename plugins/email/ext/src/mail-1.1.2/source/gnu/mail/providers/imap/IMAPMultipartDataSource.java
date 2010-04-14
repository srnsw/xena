/*
 * IMAPMultipartDataSource.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.Part;

/**
 * An IMAP multipart component.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 0.1
 */
public class IMAPMultipartDataSource
  implements MultipartDataSource
{

  /**
   * The multipart object this data source is associated with.
   */
  protected IMAPMultipart multipart;

  /**
   * Called by the IMAPMessage.
   */
  protected IMAPMultipartDataSource(IMAPMultipart multipart)
  {
    this.multipart = multipart;
  }

  /**
   * Returns the content description of the body part that contains the
   * multipart.
   */
  public String getName()
  {
    try
      {
        return multipart.getParent().getDescription();
      }
    catch (MessagingException e)
      {
        return null;
      }
  }

  /**
   * Returns the content type of the body part that contains the multipart.
   */
  public String getContentType()
  {
    try
      {
        return multipart.getParent().getContentType();
      }
    catch (MessagingException e)
      {
        return null;
      }
  }

  /**
   * Returns an input stream from which the content of this multipart can be
   * read.
   */
  public InputStream getInputStream()
    throws IOException
  {
    try
      {
        Part part = multipart.getParent();
        if (part instanceof IMAPBodyPart)
          {
            return ((IMAPBodyPart) part).getContentStream();
          }
        else if (part instanceof IMAPMessage)
          {
            return ((IMAPMessage) part).getContentStream();
          }
        else
          {
            throw new IOException("Internal error in part structure");
          }
      }
    catch (MessagingException e)
      {
        throw new IOException(e.getMessage());
      }
  }

  /**
   * IMAP multiparts are read-only.
   */
  public OutputStream getOutputStream()
    throws IOException
  {
    throw new ProtocolException("IMAP multiparts are read-only");
  }

  /**
   * Returns the secified sub-part of the multipart.
   */
  public BodyPart getBodyPart(int index)
    throws MessagingException
  {
    return multipart.getBodyPart(index);
  }

  /**
   * Returns the number of sub-parts of the multipart.
   */
  public int getCount()
  {
    try
      {
        return multipart.getCount();
      }
    catch (MessagingException e)
      {
        return 0;
      }
  }

}
