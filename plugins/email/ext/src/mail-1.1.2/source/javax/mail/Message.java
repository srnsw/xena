/*
 * Message.java
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

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import javax.mail.search.SearchTerm;

/**
 * An abstract mail message, consisting of headers and content.
 * <p>
 * A message is retrieved from a folder, and is normally a lightweight
 * object that retrieves its properties on demand. Fetch profiles may be
 * used to prefetch certain properties of a message.
 * <p>
 * To send a message, an appropriate subclsass is instantiated, its
 * properties set, and it is then delivered via a transport using the
 * <code>Transport.sendMessage</code> method.
 *
 * @see Part
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Message
  implements Part
{

  /**
   * The types of recipients to which a message can be sent.
   * The types defined here are TO, CC and BCC. Other types may be defined
   * by subclasses.
   */
  public static class RecipientType
    implements Serializable
  {

    /**
     * Primary recipients.
     */
    public static final RecipientType TO = new RecipientType("To");

    /**
     * Carbon-copy recipients.
     */
    public static final RecipientType CC = new RecipientType("Cc");

    /**
     * "Blind" carbon-copy recipients. This type of recipient is hidden from
     * other recipients of the message.
     */
    public static final RecipientType BCC = new RecipientType("Bcc");

    /**
     * The type of recipient.
     */
    protected String type;

    protected RecipientType(String type)
    {
      this.type = type;
    }
    
    protected Object readResolve()
      throws ObjectStreamException
    {
      if (type.equals("To"))
        {
          return TO;
        }
      if (type.equals("Cc"))
        {
          return CC;
        }
      if (type.equals("Bcc"))
        {
          return BCC;
        }
      throw new InvalidObjectException("Unknown RecipientType: " + type);
    }

    public String toString()
    {
      return type;
    }


  }

  /**
   * The number of this message within its folder, starting from 1, 
   * or 0 if the message was not retrieved from a folder.
   */
  protected int msgnum = 0;

  /**
   * True if this message has been expunged.
   */
  protected boolean expunged = false;

  /**
   * The containing folder.
   */
  protected Folder folder;

  /**
   * The session in scope for this message.
   */
  protected Session session;

  /**
   * Constructor with no folder or session.
   */
  protected Message()
  {
    folder = null;
    session = null;
  }

  /**
   * Constructor with a folder and a message number.
   * Used by folder implementations.
   * @param folder the containing folder
   * @param msgnum the sequence number within the folder
   */
  protected Message(Folder folder, int msgnum)
  {
    this.folder = folder;
    this.msgnum = msgnum;
    session = folder.store.session;
  }

  /**
   * Constructor with a session. Used to create messages for sending.
   * @param session the session in scope
   */
  protected Message(Session session)
  {
    folder = null;
    this.session = session;
  }

  /**
   * Returns the identity of the person(s) who ordered the sending of
   * this message.
   * <p>
   * In certain implementations, this may be different from the entity that
   * actually sent the message.
   */
  public abstract Address[] getFrom()
    throws MessagingException;

  /**
   * Sets the identity of the person sending this message, as obtained
   * from the property "mail.user".
   * If this property is absent, the system property "user.name" is used.
   * @exception IllegalWriteException if the underlying implementation does 
   * not support modification of existing values
   * @exception IllegalStateException if this message is obtained from a 
   * READ_ONLY folder
   */
  public abstract void setFrom()
    throws MessagingException;

  /**
   * Sets the identity of the person sending this message.
   * @param address the sender
   * @exception IllegalWriteException if the underlying implementation does 
   * not support modification of existing values
   * @exception IllegalStateException if this message is obtained from a 
   * READ_ONLY folder.
   */
  public abstract void setFrom(Address address)
    throws MessagingException;

  /**
   * Adds addresses to the identity of the person sending this message.
   * @param addresses the senders
   * @exception IllegalWriteException if the underlying implementation does 
   * not support modification of existing values
   * @exception IllegalStateException if this message is obtained from a 
   * READ_ONLY folder.
   */
  public abstract void addFrom(Address[] addresses)
    throws MessagingException;

  /**
   * Returns all the recipient addresses of the specified type.
   * @param type the recipient type
   */
  public abstract Address[] getRecipients(RecipientType type)
    throws MessagingException;

  /**
   * Returns all the recipient addresses in the message. 
   */
  public Address[] getAllRecipients()
    throws MessagingException
  {
    Address[] to = getRecipients(RecipientType.TO);
    Address[] cc = getRecipients(RecipientType.CC);
    Address[] bcc = getRecipients(RecipientType.BCC);
   
    if (cc == null && bcc == null)
      return to;
    
    int count = (to == null ? 0 : to.length) +
      (cc == null ? 0 : cc.length) +
      (bcc == null ? 0 : bcc.length);
    Address[] all = new Address[count];
    int offset = 0;
    if (to != null)
      {
        System.arraycopy(to, 0, all, offset, to.length);
        offset += to.length;
      }
    if (cc != null)
      {
        System.arraycopy(cc, 0, all, offset, cc.length);
        offset += cc.length;
      }
    if (bcc != null)
      {
        System.arraycopy(bcc, 0, all, offset, bcc.length);
        offset += bcc.length;
      }
    return all;
  }

  /**
   * Sets the recipient addresses of the specified type.
   * @param type the recipient type
   * @param addresses the addresses
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void setRecipients(RecipientType type, Address[] addresses)
    throws MessagingException;

  /**
   * Sets the recipient address of the specified type.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public void setRecipient(RecipientType type, Address address)
    throws MessagingException
  {
    setRecipients(type, new Address[] { address });
  }

  /**
   * Adds the recipient addresses of the given type.
   * @param type the recipient type
   * @param addresses the addresses
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void addRecipients(RecipientType type, Address[] addresses)
    throws MessagingException;

  /**
   * Adds the recipient address of the given type.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   */
  public void addRecipient(RecipientType type, Address address)
    throws MessagingException
  {
    addRecipients(type, new Address[] { address });
  }

  /**
   * Returns the addresses to which replies should be directed. This
   * defaults to the sender of the message.
   */
  public Address[] getReplyTo()
    throws MessagingException
  {
    return getFrom();
  }

  /**
   * Sets the addresses to which replies should be directed.
   */
  public void setReplyTo(Address[] addresses)
    throws MessagingException
  {
    throw new MethodNotSupportedException();
  }

  /**
   * Returns the subject of this message.
   */
  public abstract String getSubject()
    throws MessagingException;

  /**
   * Sets the subject of this message.
   * @param subject the subject
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void setSubject(String subject)
    throws MessagingException;

  /**
   * Returns the date this message was sent.
   */
  public abstract Date getSentDate()
    throws MessagingException;

  /**
   * Sets the date this message was sent.
   * @param date the sent date of this message
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void setSentDate(Date date)
    throws MessagingException;

  /**
   * Returns the date this message was received.
   */
  public abstract Date getReceivedDate()
    throws MessagingException;

  /**
   * Returns the flags for this message.
   * <p>
   * Modifying any of these flags does not affect the message flags.
   * Use the <code>setFlags</code> method to change the message's flags.
   */
  public abstract Flags getFlags()
    throws MessagingException;

  /**
   * Indicates whether the specified flag is set in this message.
   * @param flag the flag
   */
  public boolean isSet(Flags.Flag flag)
    throws MessagingException
  {
    return getFlags().contains(flag);
  }

  /**
   * Sets the specified flags on this message to the given value.
   * Any flags in this message that are not specified in the given flags
   * are unaffected.
   * @param flag the flags to be set
   * @param set the value to be set
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void setFlags(Flags flag, boolean set)
    throws MessagingException;

  /**
   * Sets the specified flag on this message to the given value.
   * @param flag the flag to be set
   * @param set the value to be set
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public void setFlag(Flags.Flag flag, boolean set)
    throws MessagingException
  {
    setFlags(new Flags(flag), set);
  }

  /**
   * Returns the message number for this message within its folder.
   * @see #msgnum 
   */
  public int getMessageNumber()
  {
    return msgnum;
  }

  /**
   * Sets the message number for this message. 
   * @see #msgnum
   */
  protected void setMessageNumber(int msgnum)
  {
    this.msgnum = msgnum;
  }

  /**
   * Returns the folder from which this message was obtained.
   */
  public Folder getFolder()
  {
    return folder;
  }

  /**
   * Indicates whether this message is expunged.
   * @see Folder#expunge
   */
  public boolean isExpunged()
  {
    return expunged;
  }

  /**
   * Sets the expunged flag for this message.
   */
  protected void setExpunged(boolean expunged)
  {
    this.expunged = expunged;
  }

  /**
   * Returns a new message suitable for a reply to this message.
   * The new message will have its recipients set appropriately, but will
   * have no content.
   * <p>
   * The subject field is filled in with the original subject prefixed with
   * "Re:" (unless it already starts with "Re:").
   * @param replyToAll if the reply should be sent to all recipients of
   * this message
   */
  public abstract Message reply(boolean replyToAll)
    throws MessagingException;

  /**
   * Save any changes made to this message into its underlying store, if
   * the message was obtained from a folder. The message may be saved
   * immediately or when its containing folder is closed.
   * <p>
   * This method ensures that any header fields are consistent with the
   * changed message contents.
   * @exception IllegalWriteException if the underlying implementation 
   * does not support modification of existing values
   * @exception IllegalStateException if this message is obtained from 
   * a READ_ONLY folder.
   */
  public abstract void saveChanges()
    throws MessagingException;

  /**
   * Indicates whether the specified search term applies to this message.
   * @param term the search term
   */
  public boolean match(SearchTerm term)
    throws MessagingException
  {
    return term.match(this);
  }

}
