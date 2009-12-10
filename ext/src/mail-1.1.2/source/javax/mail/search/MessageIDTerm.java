/*
 * MessageIDTerm.java
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

package javax.mail.search;

import javax.mail.Message;

/**
 * A comparison for the RFC822 "Message-Id", a string message identifier
 * for Internet messages that is supposed to be unique per message.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public final class MessageIDTerm
  extends StringTerm
{

  /**
   * Constructor.
   * @param msgid the Message-Id to search for
   */
  public MessageIDTerm(String msgid)
  {
    super(msgid);
  }

  /**
   * Returns true if the given message's Message-Id matches the
   * Message-Id specified in this term.
   */
  public boolean match(Message msg)
  {
    try
      {
        String[] messageIDs = msg.getHeader("Message-ID");
        if (messageIDs != null)
          {
            for (int i = 0; i < messageIDs.length; i++)
              {
                if (super.match(messageIDs[i]))
                  {
                    return true;
                  }
              }
          }
      }
    catch (Exception e)
      {
      }
    return false;
  }

  public boolean equals(Object other)
  {
    return (other instanceof MessageIDTerm && super.equals(other));
  }
  
}

