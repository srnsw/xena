/*
 * MaildirFolder.java
 * Copyright(C) 2003 Chris Burdess <dog@gnu.org>
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

package gnu.mail.providers.maildir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderEvent;
import javax.mail.internet.MimeMessage;

import gnu.inet.util.LineInputStream;

/**
 * The folder class implementing a Maildir-format mailbox.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class MaildirFolder 
  extends Folder 
{

  /**
   * Singleton instance of filter.
   */
  static final FilenameFilter filter = new MaildirFilter();

  static final String INBOX = "INBOX";

  /**
   * The maildir base directory.
   */
  File maildir;

  /**
   * The maildir <code>tmp</code> directory.
   */
  File tmpdir;

  /**
   * The maildir <code>new</code> directory.
   */
  MaildirTuple newdir;

  /**
   * The maildir <code>cur</code> directory.
   */
  MaildirTuple curdir;

  int type;
  boolean inbox;
  
  static Flags permanentFlags = new Flags();

  static long deliveryCount = 0;
		
  /**
   * Constructor.
   */
  protected MaildirFolder(Store store, String filename, boolean root,
      boolean inbox) 
  {
    super(store);
    
    maildir = new File(filename);
    tmpdir = new File(maildir, "tmp");
    newdir = new MaildirTuple(new File(maildir, "new"));
    curdir =  new MaildirTuple(new File(maildir, "cur"));
    
    mode = -1;
    type = root ? HOLDS_FOLDERS : HOLDS_MESSAGES;
    this.inbox = inbox;
  }
	
  /**
   * Constructor.
   */
  protected MaildirFolder(Store store, String filename) 
  {
    this(store, filename, false, false);
  }

  /**
   * Returns the name of this folder.
   */
  public String getName() 
  {
    if (inbox)
      return INBOX;
    return maildir.getName();
  }
	
  /**
   * Returns the full name of this folder.
   */
  public String getFullName() 
  {
    if (inbox)
      return INBOX;
    return maildir.getPath();
  }

  /**
   * Return a URLName representing this folder.
   */
  public URLName getURLName()
    throws MessagingException
  {
    URLName url = super.getURLName();
    return new URLName(url.getProtocol(), 
        null, -1, url.getFile(),
        null, null);
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
    return maildir.exists();
  }

  /**
   * Indicates whether this folder contains new messages.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean hasNewMessages() 
    throws MessagingException 
  {
    return getNewMessageCount()>0;
  }

  /**
   * Opens this folder.
   * If the folder is opened for writing, a lock must be acquired on the
   * mbox. If this fails a MessagingException is thrown.
   * @exception MessagingException if a messaging error occurred
   */
  public void open(int mode) 
    throws MessagingException 
  {
    if (this.mode!=-1)
      throw new IllegalStateException("Folder is open");
    if (!maildir.exists() || !maildir.canRead())
      throw new FolderNotFoundException(this);
    // create subdirectories if necessary
    boolean success = true;
    if (!tmpdir.exists())
      success = success && tmpdir.mkdirs();
    if (!newdir.dir.exists())
      success = success && newdir.dir.mkdirs();
    if (!curdir.dir.exists())
      success = success && curdir.dir.mkdirs();
    if (!success)
      throw new MessagingException("Unable to create directories");
    if (mode==READ_WRITE) 
    {
      if (!maildir.canWrite())
        throw new MessagingException("Folder is read-only");
    }
    // OK
    this.mode = mode;
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
    if (mode==-1)
      throw new IllegalStateException("Folder is closed");
    if (expunge)
      expunge();
    mode = -1;
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
    if (mode==-1)
      throw new IllegalStateException("Folder is closed");
    if (!exists())
      throw new FolderNotFoundException(this);
    if (mode==Folder.READ_ONLY)
      throw new IllegalWriteException();
    Message[] expunged;
    synchronized (this)
    {
      List elist = new ArrayList();
      try
      {
        // delete in new
        if (newdir.messages!=null)
        {
          int len = newdir.messages.length;
          for (int i=0; i<len; i++)
          {
            MaildirMessage message = newdir.messages[i];
            if (message.getFlags().contains(Flags.Flag.DELETED))
            {
              message.file.delete();
              elist.add(message);
            }
          }
        }
        // delete in cur
        if (curdir.messages!=null)
        {
          int len = curdir.messages.length;
          for (int i=0; i<len; i++)
          {
            MaildirMessage message = curdir.messages[i];
            if (message.getFlags().contains(Flags.Flag.DELETED))
            {
              message.file.delete();
              elist.add(message);
            }
          }
        }
      }
      catch (SecurityException e)
      {
        throw new IllegalWriteException(e.getMessage());
      }
      expunged = new Message[elist.size()];
      elist.toArray(expunged);
    }
    if (expunged.length>0)
      notifyMessageRemovedListeners(true, expunged);
    return expunged;
  }
  
  /**
   * Indicates whether this folder is open.
   */
  public boolean isOpen() 
  {
    return (mode!=-1);
  }
	
  /**
   * Returns the permanent flags for this folder.
   */
  public Flags getPermanentFlags() 
  {
    return permanentFlags;
  }

  /**
   * Returns the number of messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized int getMessageCount() 
    throws MessagingException 
  {
    statDir(curdir);
    statDir(newdir);
    return curdir.messages.length + newdir.messages.length;
  }

  /**
   * Returns the number of new messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized int getNewMessageCount()
    throws MessagingException
  {
    statDir(newdir);
    return newdir.messages.length;
  }

  /**
   * Returns the specified message number from this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized Message getMessage(int msgnum) 
    throws MessagingException 
  {
    statDir(curdir);
    statDir(newdir);
    int clen = curdir.messages.length;
    int alen = clen+newdir.messages.length;
    int index = msgnum-1;
    if (index<0 || index>=alen)
      throw new MessagingException("No such message: "+msgnum);
    if (index<clen)
      return curdir.messages[index];
    else
      return newdir.messages[index-clen];
  }
	
  /**
   * Returns the messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized Message[] getMessages() 
    throws MessagingException 
  {
    statDir(curdir);
    statDir(newdir);
    int clen = curdir.messages.length;
    int nlen = newdir.messages.length;
    int alen = clen+nlen;
    Message[] m = new Message[alen];
    System.arraycopy(curdir.messages, 0, m, 0, clen);
    System.arraycopy(newdir.messages, 0, m, clen, nlen);
    return m;
  }

  /**
   * Check the specified directory for messages,
   * repopulating its <code>messages</code> member if necessary,
   * and updating its timestamp.
   */
  void statDir(MaildirTuple dir)
    throws MessagingException
  {
    long timestamp = dir.dir.lastModified();
    if (timestamp==dir.timestamp)
      return;
    File[] files = dir.dir.listFiles(filter);
    int mlen = files.length;
    dir.messages = new MaildirMessage[mlen];
    for (int i=0; i<mlen; i++)
    {
      File file = files[i];
      String uniq = file.getName();
      String info = null;
      int ci = uniq.indexOf(':');
      if (ci!=-1)
      {
        info = uniq.substring(ci+1);
        uniq = uniq.substring(0, ci);
      }
      dir.messages[i] = new MaildirMessage(this, file, uniq, info, i+1);
    }
    dir.timestamp = timestamp;
  }

  /**
   * Move the specified message between new and cur,
   * depending on whether it has been seen or not.
   */
  void setSeen(MaildirMessage message, boolean seen)
    throws MessagingException
  {
    File src = message.file;
    File dst = null;
    if (seen)
    {
      String dstname = new StringBuffer(message.uniq)
        .append(':')
        .append(message.getInfo())
        .toString();
      dst = new File(curdir.dir, dstname);
    }
    else
      dst = new File(newdir.dir, message.uniq);
    if (!src.renameTo(dst))
      throw new MessagingException("Unable to move message");
  }

  /**
   * Appends messages to this folder.
   * Only MimeMessages within the array will be appended, as we don't know
   * how to retrieve internet content for other kinds.
   * @param m an array of messages to be appended
   */
  public synchronized void appendMessages(Message[] m) 
    throws MessagingException 
  {
    MaildirMessage[] n;
    synchronized (this)
    {
      // make sure our message counts are up to date
      statDir(newdir);
      statDir(curdir);
      int nlen = newdir.messages.length;
      int clen = curdir.messages.length;
      
      List appended = new ArrayList(m.length);
      for (int i=0; i<m.length; i++) 
      {
        if (m[i] instanceof MimeMessage) 
        {
          MimeMessage src = (MimeMessage)m[i];
          Flags flags = src.getFlags();
          boolean seen = flags.contains(Flags.Flag.SEEN);
          int count = seen ? ++clen : ++nlen;
          try
          {
            String uniq = createUniq();
            String tmpname = uniq;
            String info = null;
            if (seen)
            {
              info = MaildirMessage.getInfo(flags);
              tmpname = new StringBuffer(uniq)
                .append(':')
                .append(info)
                .toString();
            }
            File tmpfile = new File(tmpdir, tmpname);
            long time = System.currentTimeMillis();
            long timeout = time + 86400000L; // 24h
            while (time<timeout)
            {
              if (!tmpfile.exists())
                break;
               try
               {
                 wait(2000); // sleep for 2s
               } 
               catch (InterruptedException e)
               {
               }
              time = System.currentTimeMillis();
            }
            if (!tmpfile.createNewFile()) // create tmp/tmpname
              throw new MessagingException("Temporary file already exists");
            OutputStream out =
              new BufferedOutputStream(new FileOutputStream(tmpfile));
            src.writeTo(out); // write message to tmp/tmpname
            out.close(); // flush and close
            File file = new File(seen ? curdir.dir : newdir.dir, tmpname);
            tmpfile.renameTo(file); // link to final location
            tmpfile.delete(); // delete temporary file
            MaildirMessage dst =
              new MaildirMessage(this, file, uniq, info, count);
            appended.add(dst);
          }
          catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
          catch (SecurityException e)
          {
            throw new IllegalWriteException(e.getMessage());
          }
        }
      }
      n = new MaildirMessage[appended.size()];
      if (n.length>0) 
        appended.toArray(n);
    }
    // propagate event
    if (n.length>0)
      notifyMessageAddedListeners(n);
  }

  /**
   * Create a unique filename.
   */
  static String createUniq()
    throws MessagingException, IOException
  {
    long time = System.currentTimeMillis() / 1000L;
    long pid = 0;
    File urandom = new File("/dev/urandom");
    if (urandom.exists() && urandom.canRead())
    {
      // Read 8 bytes from /dev/urandom
      byte[] bytes = new byte[8];
      InputStream in = new FileInputStream(urandom);
      int offset = 0;
      while (offset<bytes.length)
        offset += in.read(bytes, offset, bytes.length-offset);
      in.close();
      for (int i=0; i<bytes.length; i++)
        pid |= (((long)bytes[i]) *((long)Math.pow(i, 2)));
    }
    else
      pid += ++deliveryCount;
    String host = InetAddress.getLocalHost().getHostName();
    return new StringBuffer()
      .append(time)
      .append('.')
      .append(pid)
      .append('.')
      .append(host)
      .toString();
  }

  /**
   * Returns the parent folder.
   */
  public Folder getParent() 
    throws MessagingException 
  {
    return store.getFolder(maildir.getParent());
  }

  /**
   * Returns the subfolders of this folder.
   */
  public Folder[] list() 
    throws MessagingException 
  {
    if (type!=HOLDS_FOLDERS)
      throw new MessagingException("This folder can't contain subfolders");
    try 
    {
      String[] files = maildir.list();
      Folder[] folders = new Folder[files.length];
      for (int i=0; i<files.length; i++)
      folders[i] =
        store.getFolder(maildir.getAbsolutePath()+File.separator+files[i]);
      return folders;
    }
    catch (SecurityException e) 
    {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Returns the subfolders of this folder matching the specified pattern.
   */
  public Folder[] list(String pattern) 
    throws MessagingException 
  {
    if (type!=HOLDS_FOLDERS)
      throw new MessagingException("This folder can't contain subfolders");
    try 
    {
      String[] files = maildir.list(new MaildirListFilter(pattern));
      Folder[] folders = new Folder[files.length];
      for (int i=0; i<files.length; i++)
      folders[i] =
        store.getFolder(maildir.getAbsolutePath()+File.separator+files[i]);
      return folders;
    } 
    catch (SecurityException e) 
    {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Returns the separator character.
   */
  public char getSeparator() 
    throws MessagingException 
  {
    return File.separatorChar;
  }

  /**
   * Creates this folder in the store.
   */
  public boolean create(int type) 
    throws MessagingException 
  {
    if (maildir.exists())
      throw new MessagingException("Folder already exists");
    switch (type) 
    {
      case HOLDS_FOLDERS:
        try 
        {
          if (!maildir.mkdirs())
            return false;
          this.type = type;
          notifyFolderListeners(FolderEvent.CREATED);
          return true;
        }
        catch (SecurityException e) 
        {
          throw new MessagingException("Access denied", e);
        }
      case HOLDS_MESSAGES:
        try 
        {
          boolean success = true;
          synchronized (this) 
          {
            success = success && maildir.mkdirs();
            success = success && tmpdir.mkdirs();
            success = success && newdir.dir.mkdirs();
            success = success && curdir.dir.mkdirs();
          }
          if (!success)
            return false;
          this.type = type;
          notifyFolderListeners(FolderEvent.CREATED);
          return true;
        } 
        catch (SecurityException e) 
        {
          throw new MessagingException("Access denied", e);
        }
    }
    return false;
  }

  /**
   * Deletes this folder.
   */
  public boolean delete(boolean recurse) 
    throws MessagingException 
  {
    if (recurse) 
    {
      try 
      {
        if (type==HOLDS_FOLDERS) 
        {
          Folder[] folders = list();
          for (int i=0; i<folders.length; i++)
            if (!folders[i].delete(recurse))
              return false;
        }
        if (!delete(maildir))
          return false;
        notifyFolderListeners(FolderEvent.DELETED);
        return true;
      } 
      catch (SecurityException e) 
      {
        throw new MessagingException("Access denied", e);
      }
    } 
    else 
    {
      try 
      {
        if (type==HOLDS_FOLDERS) 
        {
          Folder[] folders = list();
          if (folders.length>0)
            return false;
        }
        if (!delete(maildir))
          return false;
        notifyFolderListeners(FolderEvent.DELETED);
        return true;
      } 
      catch (SecurityException e) 
      {
        throw new MessagingException("Access denied", e);
      }
    }
  }

  /**
   * Depth-first file/directory delete.
   */
  boolean delete(File file)
    throws SecurityException
  {
    if (file.isDirectory())
    {
      File[] files = file.listFiles();
      for (int i=0; i<files.length; i++)
      {
        if (!delete(files[i]))
          return false;
      }
    }
    return file.delete();
  }

  /**
   * Renames this folder.
   */
  public boolean renameTo(Folder folder) 
    throws MessagingException 
  {
    try 
    {
      String filename = folder.getFullName();
      if (filename!=null) 
      {
        if (!maildir.renameTo(new File(filename)))
          return false;
        notifyFolderRenamedListeners(folder);
        return true;
      } 
      else
        throw new MessagingException("Illegal filename: null");
    } 
    catch (SecurityException e) 
    {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Returns the subfolder of this folder with the specified name.
   */
  public Folder getFolder(String filename) 
    throws MessagingException 
  {
    if (INBOX.equalsIgnoreCase(filename))
    {
      try
      {
        return store.getFolder(INBOX);
      }
      catch (MessagingException e)
      {
        // fall back to standard behaviour
      }
    }
    return store.getFolder(maildir.getAbsolutePath()+File.separator+filename);
  }

  /**
   * Filename filter that rejects dotfiles.
   */
  static class MaildirFilter
    implements FilenameFilter
  {

    public boolean accept(File dir, String name)
    {
      return name.length()>0 && name.charAt(0)!=0x2e;
    }
    
  }

  /**
   * Structure holding the details for a maildir subdirectory.
   */
  static class MaildirTuple
  {
    
    File dir;
    long timestamp = 0L;
    MaildirMessage[] messages = null;

    MaildirTuple(File dir)
    {
      this.dir = dir;
    }
    
  }

  /**
   * Filename filter for listing subfolders.
   */
  class MaildirListFilter 
    implements FilenameFilter 
  {

    String pattern;
    int asteriskIndex, percentIndex;
	   
    MaildirListFilter(String pattern) 
    {
      this.pattern = pattern;
      asteriskIndex = pattern.indexOf('*');
      percentIndex = pattern.indexOf('%');
    }
	   
    public boolean accept(File directory, String name) 
    {
      if (asteriskIndex>-1) 
      {
        String start = pattern.substring(0, asteriskIndex);
        String end = pattern.substring(asteriskIndex+1, pattern.length());
        return (name.startsWith(start) &&
            name.endsWith(end));
      } 
      else if (percentIndex>-1) 
      {
        String start = pattern.substring(0, percentIndex);
        String end = pattern.substring(percentIndex+1, pattern.length());
        return (directory.equals(maildir) &&
            name.startsWith(start) &&
            name.endsWith(end));
      }
      return name.equals(pattern);
    }
  }
    
}
