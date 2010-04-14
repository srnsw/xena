/*
 * Store.java
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

import java.util.ArrayList;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

/**
 * A message store that can be used to save and retrieve messages.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Store 
  extends Service
{

  private ArrayList storeListeners = null;
  private ArrayList folderListeners = null;

  /**
   * Constructor.
   * @param session session context for this store
   * @param url URLName to be used for this store
   */
  protected Store(Session session, URLName url)
  {
    super(session, url);
  }

  /**
   * Returns a folder that represents the root of the primary namespace
   * presented to the user by this store.
   * @exception IllegalStateException if the store is not connected
   */
  public abstract Folder getDefaultFolder()
    throws MessagingException;

  /**
   * Returns the folder with the given name.
   * <p>
   * The <code>exists</code> method can be used to determine whether the
   * folder actually exists.
   * <p>
   * In some Stores, <code>name</code> can be an absolute path if it starts
   * with the hierarchy delimiter. Otherwise it is interpreted relative
   * to the root of this namespace.
   * @param name the folder name
   * @exception IllegalStateException if the store is not connected
   */
  public abstract Folder getFolder(String name)
    throws MessagingException;

  /**
   * Returns the folder corresponding to the given URLName.
   * @param url a URLName denoting a folder
   * @exception IllegalStateException if this store is not connected
   */
  public abstract Folder getFolder(URLName url)
    throws MessagingException;

  /**
   * Returns the personal namespaces for the authenticated user.
   */
  public Folder[] getPersonalNamespaces()
    throws MessagingException
  {
    Folder[] folders = new Folder[1];
    folders[0] = getDefaultFolder();
    return folders;
  }

  /**
   * Returns the personal namespaces for the specified user.
   */
  public Folder[] getUserNamespaces(String user)
    throws MessagingException
  {
    return new Folder[0];
  }

  /**
   * Returns the shared namespaces.
   */
  public Folder[] getSharedNamespaces()
    throws MessagingException
  {
    return new Folder[0];
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
  
  // -- Store events --

  /**
   * Adds a listener for store events on this store.
   */
  public void addStoreListener(StoreListener l)
  {
    if (storeListeners == null)
      {
        storeListeners = new ArrayList();
      }
    synchronized (storeListeners)
      {
        storeListeners.add(l);
      }
  }

  /**
   * Removes a store events listener.
   */
  public void removeStoreListener(StoreListener l)
  {
    if (storeListeners != null)
      {
        synchronized (storeListeners)
          {
            storeListeners.remove(l);
          }
      }
  }

  /**
   * Notifies all store event listeners.
   */
  protected void notifyStoreListeners(int type, String message)
  {
    StoreEvent event = new StoreEvent(this, type, message);
    fireNotification(event);
  }

  /*
   * Propagates a StoreEvent to all registered listeners.
   */
  void fireNotification(StoreEvent event)
  {
    if (storeListeners != null)
      {
        StoreListener[] l = null;
        synchronized (storeListeners)
          {
            l = new StoreListener[storeListeners.size()];
            storeListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].notification(event);
          }
      }
  }

  // -- Folder events --

  /**
   * Adds a listener for folder events on any folder object obtained from this
   * store.
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
   * Removes a folder event listener.
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
   * Notifies all folder listeners.
   */
  protected void notifyFolderListeners(int type, Folder folder)
  {
    FolderEvent event = new FolderEvent(this, folder, type);
    switch (type)
      {
      case FolderEvent.CREATED:
        fireFolderCreated(event);
        break;
      case FolderEvent.DELETED:
        fireFolderDeleted(event);
        break;
      }
  }

  /**
   * Notifies all folder listeners about the renaming of a folder.
   */
  protected void notifyFolderRenamedListeners(Folder oldFolder, 
                                               Folder newFolder)
  {
    FolderEvent event = new FolderEvent(this, oldFolder, newFolder, 
                                         FolderEvent.RENAMED);
    fireFolderRenamed(event);
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

}

