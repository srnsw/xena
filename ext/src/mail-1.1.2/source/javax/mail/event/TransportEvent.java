/*
 * TransportEvent.java
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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;

/**
 * A transport event.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class TransportEvent
  extends MailEvent
{

  /**
   * The message was successfully delivered to all recipients.
   */
  public static final int MESSAGE_DELIVERED = 1;

  /**
   * The message was not sent. 
   */
  public static final int MESSAGE_NOT_DELIVERED = 2;

  /**
   * The message was successfully sent to some but not all of the recipients.
   */
  public static final int MESSAGE_PARTIALLY_DELIVERED = 3;

  /**
   * The event type.
   */
  protected int type;

  protected transient Address[] validSent;
  protected transient Address[] validUnsent;
  protected transient Address[] invalid;
  protected transient Message msg;

  /**
   * Constructor.
   * @param source the transport
   * @param type the event type
   * @param validSent the valid sent addresses
   * @param validUnsent the valid unsent addresses
   * @param invalid the invalid addresses
   * @param msg the message
   */
  public TransportEvent(Transport transport, int type,
                        Address[] validSent, Address[] validUnsent,
                        Address[] invalid, Message msg)
  {
    super(transport);
    this.type = type;
    this.validSent = validSent;
    this.validUnsent = validUnsent;
    this.invalid = invalid;
    this.msg = msg;
  }

  /**
   * Returns the type of this event.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Returns the addresses to which this message was delivered succesfully.
   */
  public Address[] getValidSentAddresses()
  {
    return validSent;
  }

  /**
   * Returns the addresses that are valid but to which this message was not 
   * delivered.
   */
  public Address[] getValidUnsentAddresses()
  {
    return validUnsent;
  }

  /**
   * Returns the addresses to which this message could not be sent.
   */
  public Address[] getInvalidAddresses()
  {
    return invalid;
  }

  /**
   * Returns the message.
   */
  public Message getMessage()
  {
    return msg;
  }

  /**
   * Invokes the appropriate listener method.
   */
  public void dispatch(Object listener)
  {
    TransportListener l = (TransportListener) listener;
    switch (type)
    {
      case MESSAGE_DELIVERED:
        l.messageDelivered(this);
        break;
      case MESSAGE_NOT_DELIVERED:
        l.messageNotDelivered(this);
        break;
      case MESSAGE_PARTIALLY_DELIVERED:
        l.messagePartiallyDelivered(this);
        break;
    }
  }

}

