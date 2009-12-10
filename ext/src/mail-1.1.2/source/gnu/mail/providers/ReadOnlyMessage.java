/*
 * ReadOnlyMessage.java
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

package gnu.mail.providers;

import java.io.InputStream;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;

/**
 * Abstract read-only message.
 * The superclass of mail provider messages that do not support message
 * editing in-place.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0
 */
public abstract class ReadOnlyMessage extends MimeMessage
{

  protected ReadOnlyMessage(Folder folder, int msgnum) 
    throws MessagingException 
  {
    super(folder, msgnum);
  }

  protected ReadOnlyMessage(Folder folder, InputStream in, int msgnum) 
    throws MessagingException 
  {
    super(folder, in, msgnum);
  }

  protected ReadOnlyMessage(Folder folder, InternetHeaders headers,
      byte[] content, int msgnum) 
    throws MessagingException 
  {
    super(folder, msgnum);
  }

  protected ReadOnlyMessage(MimeMessage message)
    throws MessagingException
  {
    super(message);
  }

  // -- content --

  public void setContent(Object o, String type)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  public void setContent(Multipart mp)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  // -- headers --

  public void setHeader(String name, String value)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  public void addHeader(String name, String value)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  public void removeHeader(String name)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  public void addHeaderLine(String line)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  // -- flags --
  
  public void setFlags(Flags flag, boolean set)
    throws MessagingException
  {
    throw new IllegalWriteException();
  }

  // -- general --

  public void saveChanges()
    throws MessagingException
  {
  }

}
