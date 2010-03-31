/*
 * MboxStore.java
 * Copyright(C) 1999,2005 Chris Burdess <dog@gnu.org>
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
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

package gnu.mail.providers.mbox;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import gnu.inet.util.GetSystemPropertyAction;
import gnu.inet.util.TraceLevel;
import gnu.mail.treeutil.StatusEvent;
import gnu.mail.treeutil.StatusListener;
import gnu.mail.treeutil.StatusSource;

/**
 * The storage class implementing the Mbox mailbox file format.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class MboxStore 
  extends Store 
  implements StatusSource
{

  static final Logger logger =
    Logger.getLogger("gnu.mail.util.providers.mbox");

  static final Level MBOX_TRACE = new TraceLevel("mbox");

  private static final char separatorChar = '/';
  
  static boolean attemptFallback = true;

  /**
   * File representing the root of this store's hierarchy.
   */
  File root;
  
  private List statusListeners = new ArrayList();
	
  /**
   * Constructor.
   */
  public MboxStore(Session session, URLName urlname) 
  {
    super(session, urlname);
    String af = session.getProperty("mail.mbox.attemptfallback");
    if (af != null) 
      {
        attemptFallback = Boolean.valueOf(af).booleanValue();
      }
    if (session.getDebug())
      {
        logger.setLevel(MBOX_TRACE);
      }
  }
  
  /**
   * There isn't a protocol to implement, so this method just returns.
   */
  protected boolean protocolConnect(String host, 
                                    int port, 
                                    String username,
                                    String password) 
    throws MessagingException 
  {
    if (url != null)
      {
        String path = url.getFile();
        if (path != null && !"".equals(path))
          {
            path = decodeUrlPath(path);
            
            // Relative or Windows absolute path
            if (File.separatorChar != '/')
              {
                path = path.replace('/', File.separatorChar);
              }
            root = new File(path);
            if (!root.exists() && File.separatorChar == '/')
              {
                // Absolute path on POSIX platform
                root = new File("/" + path);
              }
          }
      }
    if (root == null)
      {
        String mailhome = session.getProperty("mail.mbox.mailhome");
        if (mailhome != null)
          {
            root = new File(mailhome);
          }
      }
    if (root == null)
      {
        PrivilegedAction a = new GetSystemPropertyAction("user.name");
        String userhome = (String) AccessController.doPrivileged(a);
        root = new File(userhome, "Mail"); // elm
        if (!root.exists())
          {
            root = new File(userhome, "mail");
            if (!root.exists())
              {
                root = null;
              }
          }
      }
    return true;
  }

  /**
   * Sets the correct form of the URLName.
   */
  protected void setURLName(URLName url)
  {
    url = new URLName(url.getProtocol(), null, -1, url.getFile(), null, null);
    super.setURLName(url);
  }
  
  /**
   * Returns the default folder.
   */
  public Folder getDefaultFolder()
    throws MessagingException
  {
    return getFolder("");
  }

  /**
   * Returns the folder with the specified filename.
   */
  public Folder getFolder(String name)
    throws MessagingException
  {
    return getFolder(name, false);
  }

  /**
   * Returns the folder specified by the filename of the URLName.
   */
  public Folder getFolder(URLName urlname) 
    throws MessagingException 
  {
    String path = urlname.getFile();
    if (path != null)
      {
        path = decodeUrlPath(path);
      }
    return getFolder(path, true);
  }
  
  private Folder getFolder(String name, boolean tryPrepend)
    throws MessagingException
  {
    if (File.separatorChar == '\\' && name != null &&
        name.startsWith("\\\\\\"))
      {
        // Remove spurious leading backslashes for Windows absolute
        // pathnames created by MboxFolder.getFullPath
        name = name.substring(3);
      }
    if (name == null || "".equals(name))
      {
        // Default folder
        return (root != null) ? new MboxFolder(this, root, false) : null;
      }
    File file = null;
    // Convert any slashes to platform path separator
    if (File.separatorChar != '/')
      {
        name = name.replace('/', File.separatorChar);
      }
    if (root != null && root.isDirectory())
      {
        file = new File(root, name);
      }
    if (file == null || !file.exists())
      {
        // Relative or absolute path
        file = new File(name);
        if (!file.exists() && tryPrepend)
          {
            file = new File(File.separator + name);
          }
      }
    if ("INBOX".equalsIgnoreCase(name) && !file.exists())
      {
        File inbox = file;
        // If the root is a file try that first.
        if (root != null && root.isFile())
          {
            inbox = root;
          }
        if (!inbox.exists())
          {
            // Try the session property mail.mbox.inbox.
            String inboxname = session.getProperty("mail.mbox.inbox");
            if (inboxname != null)
              {
                inbox = new File(inboxname);
              }
          }
        if (!inbox.exists() && attemptFallback && File.separatorChar == '/') 
          {
            PrivilegedAction a;
            if (File.separatorChar == '/')
              {
                // Try some common (UNIX) locations.
                a = new GetSystemPropertyAction("user.name");
                String username = (String) AccessController.doPrivileged(a);
                inbox = new File("/var/mail/" + username); // GNU
                if (!inbox.exists())
                  {
                    inbox = new File("/var/spool/mail/" + username);
                    // common alternative
                  }
              }
            if (!inbox.exists())
              {
                a = new GetSystemPropertyAction("user.home");
                String userhome = (String) AccessController.doPrivileged(a);
                inbox = new File(userhome, "Mailbox"); // qmail etc
              }
          }
        return new MboxFolder(this, inbox, true);
      }
    return new MboxFolder(this, file, false);
  }

  Session getSession() 
  {
    return session;
  }

  /**
   * Print a log message.
   */
  void log(String message)
  {
    logger.log(MBOX_TRACE, message);
  }

  // -- StatusSource --

  /**
   * Adds a status listener to this store.
   * The listener will be informed of state changes during potentially
   * lengthy procedures(opening and closing mboxes).
   * @param l the status listener
   * @see #removeStatusListener
   */
  public void addStatusListener(StatusListener l) 
  {
    synchronized (statusListeners) 
      {
        statusListeners.add(l);
      }
  }
			
  /**
   * Removes a status listener from this store.
   * @param l the status listener
   * @see #addStatusListener
   */
  public void removeStatusListener(StatusListener l) 
  {
    synchronized (statusListeners) 
      {
        statusListeners.remove(l);
      }
  }

  /**
   * Processes a status event.
   * This dispatches the event to all the registered listeners.
   * @param event the status event
   */
  protected void processStatusEvent(StatusEvent event) 
  {
    StatusListener[] listeners;
    synchronized (statusListeners) 
      {
        listeners = new StatusListener[statusListeners.size()];
        statusListeners.toArray(listeners);
      }
    switch (event.getType()) 
      {
      case StatusEvent.OPERATION_START:
        for (int i = 0; i < listeners.length; i++)
          {
            listeners[i].statusOperationStarted(event);
          }
        break;
      case StatusEvent.OPERATION_UPDATE:
        for (int i = 0; i < listeners.length; i++)
          {
            listeners[i].statusProgressUpdate(event);
          }
        break;
      case StatusEvent.OPERATION_END:
        for (int i = 0; i < listeners.length; i++)
          {
            listeners[i].statusOperationEnded(event);
          }
        break;
    }
  }

  static String decodeUrlPath(String path)
  {
    boolean hi = false;
    StringBuffer buf = null;
    int len = path.length();
    for (int i = 0; i < len; i++)
      {
        char c = path.charAt(i);
        if (c == '%' && i < (len - 2))
          {
            if (buf == null)
              {
                buf = new StringBuffer(path.substring(0, i));
              }
            int code = Integer.parseInt(path.substring(i + 1, i + 3), 16);
            if (code > 127)
              {
                hi = true;
              }
            buf.append((char) code);
            i += 2;
          }
        else if (buf != null)
          {
            buf.append(c);
          }
      }
    if (buf != null)
      {
        if (!hi)
          {
            // ASCII only
            return buf.toString();
          }
        // decode UTF-8 sequence
        len = buf.length();
        byte[] seq = new byte[len];
        for (int i = 0; i < len; i++)
          {
            seq[i] = (byte) buf.charAt(i);
          }
        try
          {
            return new String(seq, "UTF-8");
          }
        catch (UnsupportedEncodingException e)
          {
            RuntimeException e2 = new RuntimeException();
            e2.initCause(e);
            throw e2;
          }
      }
    // No encoded characters
    return path;
  }

  static String encodeUrlPath(String path)
  {
    int len = path.length();
    StringBuffer buf = null;
    for (int i = 0; i < len; i++)
      {
        char c = path.charAt(i);
        if (!isUnreservedPathChar(c))
          {
            if (buf == null)
              {
                buf = new StringBuffer(path.substring(0, i));
              }
            try
              {
                // UTF-8 sequence for character
                byte[] seq = path.substring(i, i + 1).getBytes("UTF-8");
                for (int j = 0; j < seq.length; j++)
                  {
                    String code = Integer.toHexString(seq[j]).toUpperCase();
                    buf.append('%');
                    if (code.length() < 2)
                      {
                        buf.append('0');
                      }
                    buf.append(code);
                  }
                return buf.toString();
              }
            catch (UnsupportedEncodingException e)
              {
                RuntimeException e2 = new RuntimeException();
                e2.initCause(e);
                throw e2;
              }
          }
        else if (buf != null)
          {
            buf.append(c);
          }
      }
    // ASCII only
    return path;
  }

  static boolean isUnreservedPathChar(char c)
  {
    return (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) ||
      (c >= 0x30 && c <= 0x39) || c == '-' || c == '.' || c == '_' ||
      c == '~' || c == '/';
  }

}

