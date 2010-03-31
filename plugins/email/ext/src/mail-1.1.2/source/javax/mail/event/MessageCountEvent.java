/*
 * MessageCountEvent.java
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

package javax.mail.event;

import javax.mail.Folder;
import javax.mail.Message;

/**
 * A change in the number of messages in a folder.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class MessageCountEvent
  extends MailEvent
{

  /**
   * Messages were added to the folder.
   */
  public static final int ADDED = 1;

  /**
   * Messages were removed from the folder.
   */
  public static final int REMOVED = 2;

  /**
   * The event type.
   */
  protected int type;

  /**
   * If true, this event is the result of an explicit expunge by this client.
   * Otherwise this event is the result of an expunge by external mechanisms.
   */
  protected boolean removed;

  /**
   * The messages.
   */
  protected transient Message[] msgs;

  /**
   * Constructor.
   * @param source the folder
   * @param type the event type (ADDED or REMOVED)
   * @param removed whether this event is the result of a specific expunge
   * @param msgs the messages added or removed
   */
  public MessageCountEvent(Folder source, int type, boolean removed, 
                           Message[] msgs)
  {
    super(source);
    this.type = type;
    this.removed = removed;
    this.msgs = msgs;
  }

  /**
   * Returns the type of this event.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Indicates whether this event is the result of an explicit expunge, or
   * of an expunge by an external mechanism.
   */
  public boolean isRemoved()
  {
    return removed;
  }

  /**
   * Returns the messages that were added or removed.
   */
  public Message[] getMessages()
  {
    return msgs;
  }

  /**
   * Invokes the appropriate listener method.
   */
  public void dispatch(Object listener)
  {
    MessageCountListener l = (MessageCountListener) listener;
    switch (type)
    {
      case ADDED:
        l.messagesAdded(this);
        break;
      case REMOVED:
        l.messagesRemoved(this);
        break;
    }
  }

}
