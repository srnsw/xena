/*
 * MessageContext.java
 * Copyright (C) 2002 The Free Software Foundation
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

package javax.mail;

/**
 * The context of a datum of message content.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MessageContext
{

  /**
   * The part.
   */
  private Part part;

  /**
   * Creates a message context describing the given part.
   */
  public MessageContext(Part part)
  {
    this.part = part;
  }

  /**
   * Returns the part containing the content. This may be null.
   */
  public Part getPart()
  {
    return part;
  }

  /**
   * Returns the message that contains the content.
   */
  public Message getMessage()
  {
    Part p = part;
    while (p != null)
      {
        if (p instanceof Message)
          {
            return (Message) p;
          }
        if (p instanceof BodyPart)
          {
            BodyPart bp = (BodyPart) p;
            Multipart mp = bp.getParent();
            p = mp.getParent();
          }
        else
          {
            p = null;
          }
      }
    return null;
  }

  /**
   * Returns the session context.
   */
  public Session getSession()
  {
    Message message = getMessage();
    if (message != null)
      {
        return message.session;
      }
    return null;
  }

}
