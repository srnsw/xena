/*
 * ConnectionEvent.java
 * Copyright (C) 2002 The Free Software Fooundation
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

/**
 * A connection event.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class ConnectionEvent
  extends MailEvent
{

  /**
   * A connection was opened.
   */
  public static final int OPENED = 1;

  /**
   * A connection was disconnected (not currently used).
   */
  public static final int DISCONNECTED = 2;

  /**
   * A connection was closed.
   */
  public static final int CLOSED = 3;

  /**
   * The event type.
   */
  protected int type;

  /**
   * Constructor.
   * @param source the source
   * @param type one of OPENED, DISCONNECTED, or CLOSED
   */
  public ConnectionEvent(Object source, int type)
  {
    super(source);
    this.type = type;
  }

  /**
   * Returns the type of this event.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Invokes the appropriate listener method.
   */
  public void dispatch(Object listener)
  {
    ConnectionListener l = (ConnectionListener) listener;
    switch (type)
    {
      case OPENED:
        l.opened(this);
        break;
      case DISCONNECTED:
        l.disconnected(this);
        break;
      case CLOSED:
        l.closed(this);
        break;
    }
  }
}
