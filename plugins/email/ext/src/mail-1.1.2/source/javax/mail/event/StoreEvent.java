/*
 * StoreEvent.java
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

import javax.mail.Store;

/**
 * A store notification event.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class StoreEvent
  extends MailEvent
{

  /**
   * Indicates that this message is an ALERT.
   */
  public static final int ALERT = 1;

  /**
   * Indicates that this message is a NOTICE.
   */
  public static final int NOTICE = 2;

  /**
   * The event type.
   */
  protected int type;

  /**
   * The message text to be presented to the user.
   */
  protected String message;

  /**
   * Constructor.
   * @param source the store
   * @param type the type of event (ALERT or NOTICE)
   * @param message the notification message
   */
  public StoreEvent(Store source, int type, String message)
  {
    super(source);
    this.type = type;
    this.message = message;
  }

  /**
   * Returns the type of this event.
   */
  public int getMessageType()
  {
    return type;
  }

  /**
   * Returns the notification message.
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * Invokes the appropriate listener method.
   */
  public void dispatch(Object listener)
  {
   ((StoreListener) listener).notification(this);
  }
  
}
