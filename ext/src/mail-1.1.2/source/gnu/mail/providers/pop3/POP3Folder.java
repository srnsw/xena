/*
 * POP3Folder.java
 * Copyright (C) 1999, 2003 Chris Burdess <dog@gnu.org>
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

package gnu.mail.providers.pop3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.event.ConnectionEvent;

import gnu.inet.pop3.POP3Connection;

/**
 * The folder class implementing the POP3 mail protocol.
 *
 * @author <a href='mailto:dog@dog.net.uk'>Chris Burdess</a>
 * @author <a href='mailto:nferrier@tapsellferrier.co.uk'>Nic Ferrier</a>
 * @version 1.3
 */
public final class POP3Folder 
extends Folder 
{

  boolean readonly = false, open = false;
  int type;

  Folder inbox;

  List deleted;
  
  /**
   * Constructor.
   */
  protected POP3Folder(Store store, int type) 
  {
    super(store);
    this.type = type;
  }

  /**
   * Returns the name of this folder.
   */
  public String getName() 
  {
    switch (type) 
      {
      case HOLDS_FOLDERS:
        return "/";
      case HOLDS_MESSAGES:
        return "INBOX";
      default:
        return "(Unknown)";
      }
  }

  /**
   * Returns the full name of this folder.
   */
  public String getFullName() 
  {
    return getName();
  }

  /**
   * Returns the type of this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getType() 
    throws MessagingException 
  {
    return type;
  }

  /**
   * Indicates whether this folder exists.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean exists() 
    throws MessagingException 
  {
    return (type == HOLDS_MESSAGES);
  }

  /**
   * Indicates whether this folder contains new messages.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean hasNewMessages() 
    throws MessagingException 
  {
    return getNewMessageCount() > 0;
  }

  /**
   * Opens this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public void open(int mode) 
    throws MessagingException 
  {
    switch (mode)
      {
      case READ_WRITE:
        readonly = false;
        deleted = new ArrayList();
        break;
      case READ_ONLY:
        readonly = true;
        break;
      }
    this.mode = mode;
    open = true;
    notifyConnectionListeners(ConnectionEvent.OPENED);
  }

  /**
   * Closes this folder.
   * @param expunge if the folder is to be expunged before it is closed
   * @exception MessagingException if a messaging error occurred
   */
  public void close(boolean expunge) 
    throws MessagingException 
  {
    if (!open)
      {
        throw new MessagingException("Folder is not open");
      }
    if (expunge)
      {
        expunge();
      }
    deleted = null;
    open = false;
    notifyConnectionListeners(ConnectionEvent.CLOSED);
  }

  /**
   * Expunges this folder.
   * This deletes all the messages marked as deleted.
   * @exception MessagingException if a messaging error occurred
   */
  public Message[] expunge() 
    throws MessagingException 
  {
    if (!open)
      {
        throw new MessagingException("Folder is not open");
      }
    if (readonly)
      {
        throw new MessagingException("Folder was opened read-only");
      }
    POP3Connection connection = ((POP3Store) store).connection;
    synchronized (connection)
      {
        try
          {
            for (Iterator i = deleted.iterator(); i.hasNext(); )
              {
                Message msg = (Message) i.next();
                int msgnum = msg.getMessageNumber();
                connection.dele(msgnum);
              }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
    }
    Message[] d = new Message[deleted.size()];
    deleted.toArray(d);
    deleted.clear();
    return d;
  }

  /**
   * Indicates whether this folder is open.
   */
  public boolean isOpen() 
  {
    return open;
  }

  /**
   * Returns the permanent flags for this folder.
   */
  public Flags getPermanentFlags() 
  {
    return new Flags();
  }

  /**
   * Returns the number of messages in this folder.
   * This results in a STAT call to the POP3 server, so the latest
   * count is always delivered.
   * @exception MessagingException if a messaging error occurred
   */
  public int getMessageCount() 
    throws MessagingException 
  {
    POP3Connection connection = ((POP3Store) store).connection;
    synchronized (connection)
      {
        try
          {
            return connection.stat();
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Returns the specified message from this folder.
   * @param msgnum the message number
   * @exception MessagingException if a messaging error occurred
   */
  public Message getMessage(int msgnum) 
    throws MessagingException 
  {
    if (!open)
      {
        throw new MessagingException("Folder is not open");
      }
    POP3Connection connection = ((POP3Store) store).connection;
    synchronized (connection)
      {
        try
          {
            int size = connection.list(msgnum);
            return new POP3Message(this, msgnum, size);
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * You can't append messages to a POP3 folder.
   */
  public void appendMessages(Message[] messages) 
    throws MessagingException 
  {
    throw new IllegalWriteException();
  }

  /**
   * Fetches headers and/or content for the specified messages.
   * @exception MessagingException ignore
   */
  public void fetch(Message[] messages, FetchProfile fp) 
    throws MessagingException 
  {
    // Determine whether to fetch headers or content
    boolean fetchHeaders = false;
    boolean fetchContent = false;
    boolean fetchUid = false;
    FetchProfile.Item[] items = fp.getItems();
    for (int i = 0; i < items.length; i++)
      {
        if (items[i] == UIDFolder.FetchProfileItem.UID)
          {
            fetchUid = true;
          }
        else if (items[i] == FetchProfile.Item.CONTENT_INFO)
          {
            fetchContent = true;
          }
        else
          {
            fetchHeaders = true;
          }
      }
    if (fp.getHeaderNames().length > 0)
      {
        fetchHeaders = true;
      }
    if (!fetchHeaders && !fetchContent && !fetchUid)
      {
        return;
      }
    // Do fetch
    for (int i = 0; i < messages.length; i++)
      {
        if (messages[i] instanceof POP3Message)
          {
            POP3Message m = (POP3Message) messages[i];
            if (fetchUid)
              {
                m.fetchUid();
              }
            if (fetchContent)
              {
                m.fetchContent();
              }
            else
              {
                m.fetchHeaders();
              }
          }
      }
  }

  /**
   * Returns the subfolders for this folder.
   */
  public Folder[] list() 
    throws MessagingException 
  {
    switch (type)
      {
      case HOLDS_FOLDERS:
        if (inbox == null)
          {
            inbox = new POP3Folder(store, HOLDS_MESSAGES);
          }
        Folder[] folders = { inbox };
        return folders;
      default:
        throw new MessagingException("This folder can't contain subfolders");
      }
  }

  /**
   * Returns the subfolders for this folder.
   */
  public Folder[] list(String pattern) 
    throws MessagingException 
  {
    return list();
  }

  /**
   * POP3 folders can't have parents.
   */
  public Folder getParent() 
    throws MessagingException 
  {
    switch (type)
      {
      case HOLDS_MESSAGES:
        return ((POP3Store) store).root;
      default:
        return null;
      }
  }

  /**
   * POP3 folders can't contain subfolders.
   */
  public Folder getFolder(String s) 
    throws MessagingException 
  {
    switch (type)
      {
      case HOLDS_FOLDERS:
        if (inbox == null)
          {
            inbox = new POP3Folder(store, HOLDS_MESSAGES);
          }
        return inbox;
      default:
        throw new MessagingException("This folder can't contain subfolders");
      }
  }

  /**
   * Returns the path separator charcter.
   */
  public char getSeparator() 
    throws MessagingException 
  {
    return '\u0000';
  }

  // -- These must be overridden to throw exceptions --

  /**
   * POP3 folders can't be created, deleted, or renamed.
   */
  public boolean create(int i) 
    throws MessagingException 
  {
    throw new IllegalWriteException();
  }

  /**
   * POP3 folders can't be created, deleted, or renamed.
   */
  public boolean delete(boolean flag) 
    throws MessagingException 
  {
    throw new IllegalWriteException("Folder can't be deleted");
  }

  /**
   * POP3 folders can't be created, deleted, or renamed.
   */
  public boolean renameTo(Folder folder) 
    throws MessagingException 
  {
    throw new IllegalWriteException("Folder can't be renamed");
  }

  // -- UIDL --

  /**
   * Returns the unique ID for the given message, or <code>null</code> if
   * not available.
   * @param message the message
   */
  public String getUID(Message message)
    throws MessagingException
  {
    if (message instanceof POP3Message)
      {
        return ((POP3Message) message).getUID();
      }
    return null;
  }

}
