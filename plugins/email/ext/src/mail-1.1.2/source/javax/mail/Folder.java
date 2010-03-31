/*
 * Folder.java
 * Copyright (C) 2002, 2004 The Free Software Foundation
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

import java.util.ArrayList;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;

/**
 * A folder is a hierarchical messaging container in a store.
 * Folders may contain Messages, other Folders or both, depending on the
 * implementation.
 * <p>
 * Folder names are implementation dependent; the hierarchy components in a
 * folder's full name are separated by the folder's ancestors' hierarchy
 * delimiter characters.
 * <p>
 * The special (case-insensitive) folder name INBOX is reserved to mean the
 * primary folder for the authenticated user in the store. Not all stores 
 * support INBOX folders, and not all users will have an INBOX folder.
 * <p>
 * Unless documented otherwise, a folder must be opened in order to invoke
 * a method on it.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Folder
{

  /**
   * This folder can contain messages.
   */
  public static final int HOLDS_MESSAGES = 1;

  /**
   * This folder can contain other folders.
   */
  public static final int HOLDS_FOLDERS = 2;

  /**
   * This folder is read only.
   */
  public static final int READ_ONLY = 1;

  /**
   * This folder can be modified.
   */
  public static final int READ_WRITE = 2;
  
  /**
   * The parent store.
   */
  protected Store store;
  
  /**
   * The folder mode: Folder.READ_ONLY, Folder.READ_WRITE, or -1 if not known.
   */
  protected int mode = -1;

  // -- Event listener lists --

  private volatile ArrayList connectionListeners = null;
  private volatile ArrayList folderListeners = null;
  private volatile ArrayList messageCountListeners = null;
  private volatile ArrayList messageChangedListeners = null;

  /**
   * Constructor.
   * @param store the parent store
   */
  protected Folder(Store store)
  {
    this.store = store;
  }

  /**
   * Returns the name of this folder.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract String getName();

  /**
   * Returns the full name of this folder.
   * If the folder resides under the root hierarchy of its store,
   * the returned name is relative to the root.
   * Otherwise an absolute name, starting with the hierarchy delimiter,
   * is returned.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract String getFullName();

  /**
   * Return a URLName that can be used as a handle to access this folder.
   * This will not include the password used to authenticate to the store.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public URLName getURLName()
    throws MessagingException
  {
    URLName url = getStore().getURLName();
    String name = getFullName();
    return new URLName(url.getProtocol(), 
                       url.getHost(), url.getPort(), name,
                       url.getUsername(), null);
  }

  /**
   * Returns the parent store.
   * This method can be invoked on a closed folder.
   */
  public Store getStore()
  {
    return store;
  }

  /**
   * Returns the parent folder of this folder, or <code>null</code>
   * if this folder is the root of a folder hierarchy.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract Folder getParent()
    throws MessagingException;

  /**
   * Indicates whether this folder exists in the Store.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract boolean exists()
    throws MessagingException;

  /**
   * Returns a list of subfolders matching the specified pattern.
   * Patterns may contain the wildcard characters "%",
   * which matches any character except hierarchy delimiters, and "*",
   * which matches any character.
   * <p>
   * This method can be invoked on a closed folder.
   * @param pattern the match pattern
   */
  public abstract Folder[] list(String pattern)
    throws MessagingException;

  /**
   * Returns a list of subscribed subfolders matching the specified pattern. 
   * If the folder does not support subscription, returns the same as the
   * <code>list</code> method.
   * The pattern can contain wildcards.
   * <p>
   * This method can be invoked on a closed folder.
   * @param pattern the match pattern
   */
  public Folder[] listSubscribed(String pattern)
    throws MessagingException
  {
    return list(pattern);
  }

  /**
   * Returns the list of subfolders of this folder.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public Folder[] list()
    throws MessagingException
  {
    return list("%");
  }

  /**
   * Returns the list of subscribed subfolders of this folder.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public Folder[] listSubscribed()
    throws MessagingException
  {
    return listSubscribed("%");
  }

  /**
   * Return the hierarchy delimiter character for this folder.
   * This separates the full name of this folder from the names of
   * subfolders.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract char getSeparator()
    throws MessagingException;

  /**
   * Returns the type of this Folder, i.e. whether this folder can hold
   * messages or subfolders or both.
   * The returned value is an integer bitfield with the appropriate bits set.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public abstract int getType()
    throws MessagingException;

  /**
   * Create this folder in the store.
   * When this folder is created, any folders in its path 
   * that do not exist are also created.
   * <p>
   * If the creation is successful, a CREATED FolderEvent is delivered 
   * to any FolderListeners registered on this Folder and this Store.
   * @param type the desired type of the folder
   */
  public abstract boolean create(int type)
    throws MessagingException;

  /**
   * Indicates whether this folder is subscribed.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public boolean isSubscribed()
  {
    return true;
  }

  /**
   * Subscribe to or unsubscribe from this folder.
   * Not all Stores support subscription.
   * <p>
   * This method can be invoked on a closed folder.
   */
  public void setSubscribed(boolean flag)
    throws MessagingException
  {
    throw new MethodNotSupportedException();
  }

  /**
   * Indicates whether this folder has new messages.
   * <p>
   * This method can be invoked on a closed folder that can contain
   * messages.
   */
  public abstract boolean hasNewMessages()
    throws MessagingException;

  /**
   * Return a folder corresponding to the given name.
   * Note that the folder does not have to exist in the store.
   * <p>
   * In some stores, <code>name</code> can be an absolute path if it starts 
   * with the hierarchy delimiter. Otherwise, it is interpreted relative to 
   * this folder.
   * <p>
   * This method can be invoked on a closed folder.
   * @param name the name of the folder
   */
  public abstract Folder getFolder(String name)
    throws MessagingException;

  /**
   * Deletes this folder.
   * This method can only be invoked on a closed folder.
   * @param recurse delete any subfolders
   * @return true if the folder is deleted successfully, false otherwise
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is not closed 
   */
  public abstract boolean delete(boolean recurse)
    throws MessagingException;

  /**
   * Renames this folder.
   * This method can only be invoked on a closed folder.
   * @param folder a folder representing the new name for this folder
   * @return true if the folder is renamed successfully, false otherwise
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is not closed 
   */
  public abstract boolean renameTo(Folder folder)
    throws MessagingException;

  /**
   * Opens this folder.
   * This method can only be invoked on a closed folder that can contain
   * messages.
   * @param mode open the Folder READ_ONLY or READ_WRITE
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is not closed 
   */
  public abstract void open(int mode)
    throws MessagingException;

  /**
   * Closes this folder.
   * This method can only be invoked on an open folder.
   * @param expunge if true, expunge all deleted messages
   */
  public abstract void close(boolean expunge)
    throws MessagingException;

  /**
   * Indicates whether this folder is open.
   */
  public abstract boolean isOpen();

  /**
   * Return the mode this folder is open in.
   * Returns <code>Folder.READ_ONLY</code>, <code>Folder.READ_WRITE</code>,
   * or <code>-1</code> if the open mode is not known.
   * @exception IllegalStateException if this folder is not open
   */
  public int getMode()
  {
    if (!isOpen())
      {
        throw new IllegalStateException("Folder not open");
      }
    return mode;
  }

  /**
   * Returns the permanent flags supported by this folder.
   */
  public abstract Flags getPermanentFlags();

  /**
   * Returns the number of messages in this folder.
   * <p>
   * This method can be invoked on a closed folder; however,
   * note that for some stores, getting the message count can be an
   * expensive operation involving actually opening the folder.
   * In such cases, a provider can choose to return -1 here when the folder
   * is closed.
   */
  public abstract int getMessageCount()
    throws MessagingException;

  /**
   * Returns the number of new messages in this folder.
   * <p>
   * This method can be invoked on a closed folder; however,
   * note that for some stores, getting the message count can be an
   * expensive operation involving actually opening the folder.
   * In such cases, a provider can choose to return -1 here when the folder
   * is closed.
   */
  public synchronized int getNewMessageCount()
    throws MessagingException
  {
    if (!isOpen())
      {
        return -1;
      }
    int count = 0;
    int total = getMessageCount();
    for (int i = 1; i <= total; i++)
      {
        try
          {
            if (getMessage(i).isSet(Flags.Flag.RECENT))
              {
                count++;
              }
          }
        catch (MessageRemovedException e)
          {
          }
      }
    return count;
  }

  /**
   * Returns the number of unread messages in this folder.
   * <p>
   * This method can be invoked on a closed folder; however,
   * note that for some stores, getting the message count can be an
   * expensive operation involving actually opening the folder.
   * In such cases, a provider can choose to return -1 here when the folder
   * is closed.
   */
  public synchronized int getUnreadMessageCount()
    throws MessagingException
  {
    if (!isOpen())
      {
        return -1;
      }
    int count = 0;
    int total = getMessageCount();
    for (int i = 1; i <= total; i++)
      {
        try
          {
            if (!getMessage(i).isSet(Flags.Flag.SEEN))
              {
                count++;
              }
          }
        catch (MessageRemovedException e)
          {
          }
      }
    return count;
  }

  /**
   * Returns the number of deleted messages in this folder.
   * <p>
   * This method can be invoked on a closed folder; however,
   * note that for some stores, getting the message count can be an
   * expensive operation involving actually opening the folder.
   * In such cases, a provider can choose to return -1 here when the folder
   * is closed.
   * @exception FolderNotFoundException if this folder does not exist
   * @since JavaMail 1.3
   */
  public synchronized int getDeletedMessageCount()
    throws MessagingException
  {
    if (!isOpen())
      {
        return -1;
      }
    int count = 0;
    int total = getMessageCount();
    for (int i = 1; i <= total; i++)
      {
        try
          {
            if (!getMessage(i).isSet(Flags.Flag.DELETED))
              {
                count++;
              }
          }
        catch (MessageRemovedException e)
          {
          }
      }
    return count;
  }

  /**
   * Returns the message with the given number.
   * The message number is the relative position of a message in its
   * folder, starting at 1.
   * <p>
   * Note that message numbers can change within a session if the folder is 
   * expunged, therefore the use of message numbers as references to
   * messages is inadvisable.
   * @param msgnum the message number
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   * @exception IndexOutOfBoundsException if the message number is out of 
   * range
   */
  public abstract Message getMessage(int msgnum)
    throws MessagingException;

  /**
   * Returns the messages in the given range (inclusive).
   * @param start the number of the first message
   * @param end the number of the last message
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   * @exception IndexOutOfBoundsException if the start or end message 
   * numbers are out of range.
   */
  public synchronized Message[] getMessages(int start, int end)
    throws MessagingException
  {
    Message[] messages = new Message[(end - start) + 1];
    for (int i = start; i <= end; i++)
      {
        messages[i - start] = getMessage(i);
      }
    return messages;
  }

  /**
   * Returns the messages for the specified message numbers.
   * @param msgnums the array of message numbers
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   * @exception IndexOutOfBoundsException if any message number in the 
   * given array is out of range
   */
  public synchronized Message[] getMessages(int msgnums[])
    throws MessagingException
  {
    int total = msgnums.length;
    Message[] messages = new Message[total];
    for(int i = 0; i < total; i++)
      {
        messages[i] = getMessage(msgnums[i]);
      }
    return messages;
  }

  /**
   * Returns all messages in this folder.
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   */
  public synchronized Message[] getMessages()
    throws MessagingException
  {
    if (!isOpen())
      {
        throw new IllegalStateException("Folder not open");
      }
    int total = getMessageCount();
    Message[] messages = new Message[total];
    for (int i = 1; i <= total; i++)
      {
        messages[i - 1] = getMessage(i);
      }
    return messages;
  }

  /**
   * Appends the specified messages to this folder.
   * <p>
   * This method can be invoked on a closed folder.
   * @param msgs array of messages to be appended
   * @exception FolderNotFoundException if this folder does not exist
   * @exception MessagingException if the append operation failed
   */
  public abstract void appendMessages(Message[] msgs)
    throws MessagingException;

  /**
   * Fetches the items specified in the given fetch profile for the specified
   * messages.
   * @param msgs the messages to fetch the items for
   * @param fp the fetch profile
   */
  public void fetch(Message[] msgs, FetchProfile fp)
    throws MessagingException
  {
  }

  /**
   * Sets the specified flags on each specified message.
   * @param msgnums the messages
   * @param flag the flags to be set
   * @param value set the flags to this value
   * @exception IllegalStateException if this folder is closed or READ_ONLY
   */
  public synchronized void setFlags(Message[] msgs, Flags flag, boolean value)
    throws MessagingException
  {
    for (int i = 0; i < msgs.length; i++)
      {
        try
          {
            msgs[i].setFlags(flag, value);
          }
        catch (MessageRemovedException e)
          {
          }
      }
  }

  /**
   * Set the specified flags on the given range of messages (inclusive).
   * @param start the number of the first message
   * @param end the number of the last message
   * @param flag the flags to be set
   * @param value set the flags to this value
   * @exception IllegalStateException if this folder is closed or READ_ONLY
   * @exception IndexOutOfBoundsException if the start or end message 
   * numbers are out of range
   */
  public synchronized void setFlags(int start, int end, Flags flag, 
                                     boolean value)
    throws MessagingException
  {
    for (int i = start; i <= end; i++)
      {
        try
          {
            getMessage(i).setFlags(flag, value);
          }
        catch (MessageRemovedException e)
          {
          }
      }
  }

  /**
   * Sets the specified flags on each of the specified messages.
   * @param msgnums the message numbers
   * @param flag the flags to be set
   * @param value set the flags to this value
   * @exception IllegalStateException if this folder is closed or READ_ONLY
   * @exception IndexOutOfBoundsException if any message number in the 
   * given array is out of range
   */
  public synchronized void setFlags(int[] msgnums, Flags flag, boolean value)
    throws MessagingException
  {
    for (int i = 0; i < msgnums.length; i++)
      {
        try
          {
            getMessage(msgnums[i]).setFlags(flag, value);
          }
        catch (MessageRemovedException e)
          {
          }
      }
  }

  /**
   * Copies the specified messages into another folder.
   * <p>
   * The destination folder does not have to be open.
   * @param msgs the messages
   * @param folder the folder to copy the messages to
   */
  public void copyMessages(Message[] msgs, Folder folder)
    throws MessagingException
  {
    if (!folder.exists())
      {
        throw new FolderNotFoundException("Folder does not exist", folder);
      }
    boolean isOpen = folder.isOpen();
    if (!isOpen)
      {
        folder.open(Folder.READ_WRITE);
      }
    folder.appendMessages(msgs);
    if (!isOpen)
      {
        folder.close(false);
      }
  }

  /**
   * Expunges (permanently removing) all the messages marked DELETED.
   * Returns an array containing the expunged messages.
   * <p>
   * Expunge causes the renumbering of any messages with numbers higher than
   * the message number of the lowest-numbered expunged message.
   * <p>
   * After a message has been expunged, only the <code>isExpunged</code> and 
   * <code>getMessageNumber</code> methods are still valid on the 
   * corresponding Message object; other methods may throw 
   * <code>MessageRemovedException</code>.
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   */
  public abstract Message[] expunge()
    throws MessagingException;

  /**
   * Searches this folder for messages matching the specified search term.
   * Returns the matching messages.
   * @param term the search term
   * @exception SearchException if there was a problem with the search
   * @exception FolderNotFoundException if this folder does not exist
   * @exception IllegalStateException if this folder is closed
   */
  public Message[] search(SearchTerm term)
    throws MessagingException
  {
    return search(term, getMessages());
  }

  /**
   * Searches the given messages for those matching the specified search
   * term.
   * Returns the matching messages.
   * @param term the search term
   * @param msgs the messages to be searched
   * @exception SearchException if there was a problem with the search
   * @exception IllegalStateException if this folder is closed
   */
  public Message[] search(SearchTerm term, Message[] msgs)
    throws MessagingException
  {
    ArrayList acc = new ArrayList();
    for (int i = 0; i < msgs.length; i++)
      {
        try
          {
            if (msgs[i].match(term))
              {
                acc.add(msgs[i]);
              }
          }
        catch (MessageRemovedException e)
          {
          }
      }
    Message[] m = new Message[acc.size()];
    acc.toArray(m);
    return m;
  }

  // -- Event management --
  
  /*
   * Because the propagation of events of different kinds in the JavaMail
   * API is so haphazard, I have here sacrificed a small time advantage for
   * readability and consistency.
   *
   * All the various propagation methods now call a method with a name based
   * on the eventual listener method name prefixed by 'fire', as is the
   * preferred pattern for usage of the EventListenerList in Swing.
   *
   * Note that all events are currently delivered synchronously, where in
   * Sun's implementation a different thread is used for event delivery.
   * 
   * TODO Examine the impact of this.
   */

  // -- Connection events --

  /**
   * Add a listener for connection events on this folder.
   */
  public void addConnectionListener(ConnectionListener l)
  {
    if (connectionListeners == null)
      {
        connectionListeners = new ArrayList();
      }
    synchronized (connectionListeners)
      {
        connectionListeners.add(l);
      }
  }

  /**
   * Remove a connection event listener.
   */
  public void removeConnectionListener(ConnectionListener l)
  {
    if (connectionListeners != null)
      {
        synchronized (connectionListeners)
          {
            connectionListeners.remove(l);
          }
      }
  }

  /**
   * Notify all connection listeners.
   */
  protected void notifyConnectionListeners(int type)
  {
    ConnectionEvent event = new ConnectionEvent(this, type);
    switch (type)
      {
      case ConnectionEvent.OPENED:
        fireOpened(event);
        break;
      case ConnectionEvent.DISCONNECTED:
        fireDisconnected(event);
        break;
      case ConnectionEvent.CLOSED:
        fireClosed(event);
        break;
      }
  }

  /*
   * Propagates an OPENED ConnectionEvent to all registered listeners.
   */
  void fireOpened(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].opened(event);
          }
      }
  }

  /*
   * Propagates a DISCONNECTED ConnectionEvent to all registered listeners.
   */
  void fireDisconnected(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].disconnected(event);
          }
      }
  }

  /*
   * Propagates a CLOSED ConnectionEvent to all registered listeners.
   */
  void fireClosed(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].closed(event);
          }
      }
  }

  // -- Folder events --

  /**
   * Add a listener for folder events on this folder.
   */
  public void addFolderListener(FolderListener l)
  {
    if (folderListeners == null)
      {
        folderListeners = new ArrayList();
      }
    synchronized (folderListeners)
      {
        folderListeners.add(l);
      }
  }

  /**
   * Remove a folder event listener.
   */
  public void removeFolderListener(FolderListener l)
  {
    if (folderListeners != null)
      {
        synchronized (folderListeners)
          {
            folderListeners.remove(l);
          }
      }
  }

  /**
   * Notify all folder listeners registered on this Folder <em>and</em>
   * this folder's store.
   */
  protected void notifyFolderListeners(int type)
  {
    FolderEvent event = new FolderEvent(this, this, type);
    switch (type)
      {
      case FolderEvent.CREATED:
        fireFolderCreated(event);
        break;
      case FolderEvent.DELETED:
        fireFolderDeleted(event);
        break;
      }
    store.notifyFolderListeners(type, this);
  }

  /**
   * Notify all folder listeners registered on this folder <em>and</em>
   * this folder's store about the renaming of this folder.
   */
  protected void notifyFolderRenamedListeners(Folder folder)
  {
    FolderEvent event = new FolderEvent(this, this, folder, 
                                        FolderEvent.RENAMED);
    fireFolderRenamed(event);
    store.notifyFolderRenamedListeners(this, folder);
  }

  /*
   * Propagates a CREATED FolderEvent to all registered listeners.
   */
  void fireFolderCreated(FolderEvent event)
  {
    if (folderListeners != null)
      {
        FolderListener[] l = null;
        synchronized (folderListeners)
          {
            l = new FolderListener[folderListeners.size()];
            folderListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].folderCreated(event);
          }
      }
  }

  /*
   * Propagates a DELETED FolderEvent to all registered listeners.
   */
  void fireFolderDeleted(FolderEvent event)
  {
    if (folderListeners != null)
      {
        FolderListener[] l = null;
        synchronized (folderListeners)
          {
            l = new FolderListener[folderListeners.size()];
            folderListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].folderDeleted(event);
          }
      }
  }
  
  /*
   * Propagates a RENAMED FolderEvent to all registered listeners.
   */
  void fireFolderRenamed(FolderEvent event)
  {
    if (folderListeners != null)
      {
        FolderListener[] l = null;
        synchronized (folderListeners)
          {
            l = new FolderListener[folderListeners.size()];
            folderListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].folderRenamed(event);
          }
      }
  }

  // -- Message count events --

  /**
   * Add a listener for message count events on this folder.
   */
  public void addMessageCountListener(MessageCountListener l)
  {
    if (messageCountListeners == null)
      {
        messageCountListeners = new ArrayList();
      }
    synchronized (messageCountListeners)
      {
        messageCountListeners.add(l);
      }
  }

  /**
   * Remove a message count event listener.
   */
  public void removeMessageCountListener(MessageCountListener l)
  {
    if (messageCountListeners != null)
      {
        synchronized (messageCountListeners)
          {
            messageCountListeners.remove(l);
          }
      }
  }

  /**
   * Notify all message count listeners about the addition of messages
   * into this folder.
   */
  protected void notifyMessageAddedListeners(Message[] msgs)
  {
    MessageCountEvent event =
      new MessageCountEvent(this, MessageCountEvent.ADDED, false, msgs);
    fireMessagesAdded(event);
  }

  /**
   * Notify all message count listeners about the removal of messages from
   * this folder.
   */
  protected void notifyMessageRemovedListeners(boolean removed, Message[] msgs)
  {
    MessageCountEvent event =
      new MessageCountEvent(this, MessageCountEvent.REMOVED, removed, msgs);
    fireMessagesRemoved(event);
  }

  /*
   * Propagates an ADDED MessageCountEvent to all registered listeners.
   */
  void fireMessagesAdded(MessageCountEvent event)
  {
    if (messageCountListeners != null)
      {
        MessageCountListener[] l = null;
        synchronized (messageCountListeners)
          {
            l = new MessageCountListener[messageCountListeners.size()];
            messageCountListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].messagesAdded(event);
          }
      }
  }

  /*
   * Propagates a REMOVED MessageCountEvent to all registered listeners.
   */
  void fireMessagesRemoved(MessageCountEvent event)
  {
    if (messageCountListeners != null)
      {
        MessageCountListener[] l = null;
        synchronized (messageCountListeners)
          {
            l = new MessageCountListener[messageCountListeners.size()];
            messageCountListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].messagesRemoved(event);
          }
      }
  }

  // -- Message changed events --

  /**
   * Add a listener for message changed events on this folder.
   */
  public void addMessageChangedListener(MessageChangedListener l)
  {
    if (messageChangedListeners == null)
      {
        messageChangedListeners = new ArrayList();
      }
    synchronized (messageChangedListeners)
      {
        messageChangedListeners.add(l);
      }
  }

  /**
   * Remove a message changed event listener.
   */
  public void removeMessageChangedListener(MessageChangedListener l)
  {
    if (messageChangedListeners != null)
      {
        synchronized (messageChangedListeners)
          {
            messageChangedListeners.remove(l);
          }
      }
  }

  /**
   * Notify all message changed event listeners.
   */
  protected void notifyMessageChangedListeners(int type, Message msg)
  {
    MessageChangedEvent event = new MessageChangedEvent(this, type, msg);
    fireMessageChanged(event);
  }

  /*
   * Propagates a MessageChangedEvent to all registered listeners.
   */
  void fireMessageChanged(MessageChangedEvent event)
  {
    if (messageChangedListeners != null)
      {
        MessageChangedListener[] l = null;
        synchronized (messageChangedListeners)
          {
            l = new MessageChangedListener[messageChangedListeners.size()];
            messageChangedListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].messageChanged(event);
          }
      }
  }

  // -- Utility methods --

  /**
   * Returns the value of Folder.getFullName(), or, if that is null,
   * returns the default toString().
   */
  public String toString()
  {
    String name = getFullName();
    return (name != null) ? name : super.toString();
  }
  
}
