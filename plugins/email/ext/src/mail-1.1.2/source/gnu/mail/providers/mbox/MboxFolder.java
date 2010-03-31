/*
 * MboxFolder.java
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
 * 
 * Contributor(s): Daniel Thor Kristjan <danielk@cat.nyu.edu>
 *                   close and expunge clarification.
 *                 Sverre Huseby <sverrehu@online.no> gzipped mailboxes
 *                 Countach <yahoo@canberracity.org> Win32 support
 */

package gnu.mail.providers.mbox;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import gnu.inet.util.LineInputStream;
import gnu.mail.treeutil.StatusEvent;

/**
 * The folder class implementing a UNIX mbox-format mailbox.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class MboxFolder 
  extends Folder 
{

  static final DateFormat df = new SimpleDateFormat("EEE MMM d H:m:s yyyy");
  static final String GNU_MESSAGE_ID = "X-GNU-Message-Id";
  static final String FROM = "From ";
	
  final File file;
  final boolean inbox;
  MboxMessage[] messages;
  boolean open;
  boolean readOnly;
  
  Flags permanentFlags = null;
		
  /**
   * Constructor.
   */
  protected MboxFolder(Store store, File file, boolean inbox)
  {
    super(store);
    this.file = file;
    this.inbox = inbox;
    open = false;
    readOnly = true;
    messages = new MboxMessage[0];
  }
	
  /**
   * Returns the name of this folder.
   */
  public String getName() 
  {
    return inbox ? "INBOX" : file.getName();
  }
  
  /**
   * Returns the full name of this folder.
   * If the folder resides under the root hierarchy of this Store, the
   * returned name is relative to the root. Otherwise an absolute name,
   * starting with the hierarchy delimiter, is returned.
   */
  public String getFullName() 
  {
    MboxStore s = (MboxStore) store;
    File f = file;
    StringBuffer buf = new StringBuffer();
    while (f != null && !f.equals(s.root))
      {
        if (buf.length() > 0)
          {
            buf.insert(0, File.separatorChar);
          }
        buf.insert(0, f.getName());
        f = f.getParentFile();
      }
    if (f == null)
      {
        // This file is relative to the root of the store.
        // Under Windows, we have to return a name starting with the
        // hierarchy delimiter, so we will return a special sequence of 3
        // backslashes followed by the full Windows path name.
        if (File.separatorChar == '\\')
          {
            return "\\\\\\" + file.getPath();
          }
      }
    return buf.toString();
  }

  /**
   * Return a URLName representing this folder.
   */
  public URLName getURLName()
    throws MessagingException
  {
    String path = getFullName();
    if (File.separatorChar != '/')
      {
        path = path.replace(File.separatorChar, '/');
      }
    path = MboxStore.encodeUrlPath(path);
    return new URLName("mbox", null, -1, path, null, null);
  }

  /**
   * Returns the type of this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getType() 
    throws MessagingException 
  {
    if (file.exists())
      {
        return file.isDirectory() ? HOLDS_FOLDERS : HOLDS_MESSAGES;
      }
    return 0;
  }

  /**
   * Indicates whether this folder exists.
   * @exception MessagingException if a messaging error occurred
   */
  public boolean exists() 
    throws MessagingException 
  {
    return file.exists();
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
   * If the folder is opened for writing, a lock must be acquired on the
   * mbox. If this fails a MessagingException is thrown.
   * @exception MessagingException if a messaging error occurred
   */
  public void open(int mode) 
    throws MessagingException 
  {
    String filename = file.getPath();
    if (mode == READ_WRITE) 
      {
        if (!file.canWrite())
          {
            throw new MessagingException("Folder is read-only");
          }
        if (!acquireLock())
          {
            throw new MessagingException("Unable to acquire lock: " + filename);
          }
        readOnly = false;
      }

    if (!file.canRead())
      {
        throw new MessagingException("Can't read folder: " + filename);
      }

    LineInputStream in = null;
    try 
      {
        // Read messages
        MboxStore mstore = (MboxStore) this.store;
        mstore.log("reading " + filename);
        
        List acc = new ArrayList(256);
        in = new LineInputStream(getInputStream());
        int count = 1;
        String line, fromLine = null;
        ByteArrayOutputStream buf = null;
        
        // notify listeners
        StatusEvent event;
        event = new StatusEvent(mstore,
                                StatusEvent.OPERATION_START,
                                "open");
        mstore.processStatusEvent(event);
        
        for (line = in.readLine(); line != null; line = in.readLine()) 
          {
            if (line.indexOf(FROM) == 0) 
              {
                if (buf != null)
                  {
                    byte[] bytes = buf.toByteArray();
                    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
                    MboxMessage m =
                      new MboxMessage(this, fromLine, bin, count++);
                    acc.add(m);
                    
                    event = new StatusEvent(mstore,
                                            StatusEvent.OPERATION_UPDATE,
                                            "open",
                                            1,
                                            StatusEvent.UNKNOWN,
                                            count - 1);
                    mstore.processStatusEvent(event);
                  }
                fromLine = line;
                buf = new ByteArrayOutputStream();
              }
            else if (buf != null)
              {
                byte[] bytes = decodeFrom(line).getBytes();
                buf.write(bytes, 0, bytes.length);
                buf.write(10); // LF
              }
          }
        if (buf != null)
          {
            byte[] bytes = buf.toByteArray();
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            MboxMessage m = new MboxMessage(this, fromLine, bin, count++);
            acc.add(m);
            
            event = new StatusEvent(mstore,
                                    StatusEvent.OPERATION_UPDATE,
                                    "open",
                                    1,
                                    StatusEvent.UNKNOWN,
                                    count - 1);
            mstore.processStatusEvent(event);
          }
        messages = new MboxMessage[acc.size()];
        acc.toArray(messages);
        buf = null;
        acc = null;
        
        event = new StatusEvent(mstore,
                                StatusEvent.OPERATION_END,
                                "open");
        mstore.processStatusEvent(event);
        
        // OK
        open = true;
        notifyConnectionListeners(ConnectionEvent.OPENED);
      }
    catch (IOException e) 
      {
        throw new MessagingException("Unable to open folder: " + filename, e);
      }
    finally
      {
        // release any file descriptors
        try
          {
            if (in != null)
              {
                in.close();
              }
          }
        catch (IOException e)
          {
            // we tried
          }
      }
  }

  /**
   * Returns the specified line with any From_ line encoding removed.
   */
  public static String decodeFrom(String line)
  {
    if (line != null)
      {
        int len = line.length();
        for (int i = 0; i < (len - 5); i++)
          {
            char c = line.charAt(i);
            if (i > 0 &&
                (c == 'F' &&
                 line.charAt(i + 1) == 'r' &&
                 line.charAt(i + 2) == 'o' &&
                 line.charAt(i + 3) == 'm' &&
                 line.charAt(i + 4) == ' '))
              {
                return line.substring(1);
              }
            if (c != '>')
              {
                break;
              }
          }
      }
    return line;
  }
  
  /**
   * Closes this folder.
   * @param expunge if the folder is to be expunged before it is closed
   * @exception MessagingException if a messaging error occurred
   */
  public void close(boolean expunge) 
    throws MessagingException 
  {
    if (open) 
      {
        if (expunge)
          {
            expunge();
          }
    
        if (!readOnly)
          {
            // Save messages
            MboxStore mstore = (MboxStore) this.store;
            StatusEvent event;
            mstore.log("saving " + file.getAbsolutePath());
            synchronized (this) 
              {
                OutputStream os = null;
                try 
                  {
                    os = getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    MboxOutputStream mos = new MboxOutputStream(bos);
                    
                    event = new StatusEvent(mstore,
                                            StatusEvent.OPERATION_START,
                                            "close");
                    mstore.processStatusEvent(event);
                    for (int i = 0; i < messages.length; i++) 
                      {
                        String fromLine = fromLine(messages[i]);
                        bos.write(fromLine.getBytes());
                        bos.write('\n');
                        bos.flush();
                        messages[i].writeTo(mos);
                        mos.flush();
                        
                        event = new StatusEvent(mstore,
                                                StatusEvent.OPERATION_UPDATE,
                                                "close",
                                                1,
                                                messages.length,
                                                i + 1);
                        mstore.processStatusEvent(event);
                      }
                    
                    event = new StatusEvent(mstore,
                                            StatusEvent.OPERATION_END,
                                            "close");
                    mstore.processStatusEvent(event);
                  } 
                catch (IOException e) 
                  {
                    throw new MessagingException("I/O error writing mailbox",
                                                 e);
                  }
                finally
                  {
                    // close any file descriptors
                    try
                      {
                        if (os != null)
                          {
                            os.close();
                          }
                      }
                    catch (IOException e)
                      {
                        // we tried
                      }
                  }
              }
            if (!releaseLock())
              {
                mstore.log("unable to clear up lock file!");
              }
          }
        
        open = false;
        messages = new MboxMessage[0]; // release memory
        notifyConnectionListeners(ConnectionEvent.CLOSED);
      }
  }

  /**
   * Returns the From_ line for the specified mbox message.
   * If this does not already exist(the message was appended to the folder
   * since it was last opened), we will attempt to generate a suitable From_
   * line for it.
   */
  protected String fromLine(MboxMessage message)
    throws MessagingException
  {
    String fromLine = message.fromLine;
    if (fromLine == null)
      {
        StringBuffer buf = new StringBuffer("From ");
        
        String from = "-";
        try
          {
            Address[] f = message.getFrom();
            if (f != null && f.length > 0) 
              {
                if (f[0] instanceof InternetAddress)
                  {
                    from = ((InternetAddress) f[0]).getAddress();
                  }
                else
                  {
                    from = f[0].toString();
                  }
              }
          }
        catch (AddressException e)
          {
            // these things happen...
          }
        buf.append(from);
        buf.append(' ');
        
        Date date = message.getSentDate();
        if (date==null)
          {
            date = message.getReceivedDate();
          }
        if (date==null)
          {
            date = new Date();
          }
        buf.append(df.format(date));
        
        fromLine = buf.toString();
      }
    return fromLine;
  }
	
  /**
   * Expunges this folder.
   * This deletes all the messages marked as deleted.
   * @exception MessagingException if a messaging error occurred
   */
  public Message[] expunge() 
    throws MessagingException 
  {
    Message[] expunged;
    synchronized (this)
      {
        List elist = new ArrayList();
        if (open) 
          {
            List mlist = new ArrayList();
            for (int i=0; i<messages.length; i++) 
              {
                Flags flags = messages[i].getFlags();
                if (flags.contains(Flags.Flag.DELETED))
                  {
                    elist.add(messages[i]);
                    if (messages[i] instanceof MboxMessage)
                      {
                        ((MboxMessage)messages[i]).setExpunged(true);
                      }
                  }
                else
                  {
                    mlist.add(messages[i]);
                  }
              }
            messages = new MboxMessage[mlist.size()];
            mlist.toArray(messages);
          }
        expunged = new Message[elist.size()];
        elist.toArray(expunged);
      }
    if (expunged.length > 0)
      {
        notifyMessageRemovedListeners(true, expunged);
      }
    return expunged;
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
    if (permanentFlags == null) 
      {
        Flags flags = new Flags(); 
        flags.add(Flags.Flag.DELETED);
        flags.add(Flags.Flag.SEEN);
        flags.add(Flags.Flag.RECENT);
        permanentFlags = flags;
      }
    return permanentFlags;
  }
	
  /**
   * Returns the number of messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getMessageCount() 
    throws MessagingException 
  {
    return messages.length;
  }

  /**
   * Returns the specified message number from this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public Message getMessage(int msgnum) 
    throws MessagingException 
  {
    int index = msgnum-1;
    if (index < 0 || index >= messages.length)
      {
        throw new MessagingException("No such message: "+msgnum);
      }
    return messages[index];
  }
	
  /**
   * Returns the messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized Message[] getMessages() 
    throws MessagingException 
  {
    // Return a copy of the message array
    Message[] m = new Message[messages.length];
    System.arraycopy(messages, 0, m, 0, messages.length);
    return m;
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
    MboxMessage[] n;
    synchronized (this)
      {
        List appended = new ArrayList(m.length);
        int count = messages.length;
        for (int i = 0; i < m.length; i++) 
          {
            if (m[i] instanceof MimeMessage) 
              {
                MimeMessage mimem = (MimeMessage) m[i];
                MboxMessage mboxm = new MboxMessage(this, mimem, count++);
                if (mimem instanceof MboxMessage)
                  {
                    mboxm.fromLine = ((MboxMessage) mimem).fromLine;
                  }
                appended.add(mboxm);
              }
          }
        n = new MboxMessage[appended.size()];
        if (n.length>0) 
          {
            appended.toArray(n);
            // copy into the messages array
            List acc = new ArrayList(messages.length+n.length);
            acc.addAll(Arrays.asList(messages));
            acc.addAll(Arrays.asList(n));
            messages = new MboxMessage[acc.size()];
            acc.toArray(messages);
            acc = null;
          }
      }
    // propagate event
    if (n.length > 0)
      {
        notifyMessageAddedListeners(n);
      }
  }

  /**
   * Returns the parent folder.
   */
  public Folder getParent() 
    throws MessagingException 
  {
    if (inbox)
      {
        return store.getDefaultFolder();
      }
    if (file.equals(((MboxStore) store).root))
      {
        return null;
      }
    File parent = file.getParentFile();
    return new MboxFolder(store, parent, false);
  }

  /**
   * Returns the subfolders of this folder.
   */
  public Folder[] list() 
    throws MessagingException 
  {
    if (getType() != HOLDS_FOLDERS)
      {
        throw new MessagingException("This folder can't contain subfolders");
      }
    try 
      {
        File[] files = file.listFiles();
        Folder[] folders = new Folder[files.length];
        for (int i = 0; i < files.length; i++)
          {
            folders[i] = new MboxFolder(store, files[i], false);
          }
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
    if (getType() != HOLDS_FOLDERS)
      {
        throw new MessagingException("This folder can't contain subfolders");
      }
    try 
      {
        File[] files = file.listFiles(new MboxFilenameFilter(pattern));
        Folder[] folders = new Folder[files.length];
        for (int i = 0; i < files.length; i++)
          {
            folders[i] = new MboxFolder(store, files[i], false);
          }
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
    if (file.exists())
      {
        throw new MessagingException("Folder already exists");
      }
    switch (type) 
      {
      case HOLDS_FOLDERS:
        try 
          {
            if (!file.mkdirs())
              {
                return false;
              }
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
            File parent = file.getParentFile();
            if (!parent.exists())
              {
                if (!parent.mkdirs())
                  {
                    return false;
                  }
              }
            synchronized (this) 
              {
                createNewFile(file);
              }
            notifyFolderListeners(FolderEvent.CREATED);
            return true;
          } 
        catch (IOException e) 
          {
            throw new MessagingException("I/O error writing mailbox", e);
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
            if (file.isDirectory()) 
              {
                Folder[] folders = list();
                for (int i = 0; i < folders.length; i++)
                  {
                    if (!folders[i].delete(recurse))
                      {
                        return false;
                      }
                  }
              }
            if (!readOnly)
              {
                releaseLock();
              }
            if (!file.delete())
              {
                return false;
              }
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
            if (file.isDirectory())
              {
                Folder[] folders = list();
                if (folders.length > 0)
                  {
                    return false;
                  }
              }
            if (!readOnly)
              {
                releaseLock();
              }
            if (!file.delete())
              {
                return false;
              }
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
   * Renames this folder.
   */
  public boolean renameTo(Folder folder) 
    throws MessagingException 
  {
    if (folder instanceof MboxFolder)
      {
        File newfile = ((MboxFolder) folder).file;
        if (!file.renameTo(newfile))
          {
            return false;
          }
        notifyFolderRenamedListeners(folder);
        return true;
      } 
    else
      {
        throw new MessagingException("Target not an MboxFolder");
      }
  }

  /**
   * Returns the subfolder of this folder with the specified name.
   */
  public Folder getFolder(String name)
    throws MessagingException 
  {
    if (!file.isDirectory())
      {
        throw new MessagingException("Folder cannot contain folders");
      }
    // Convert any slashes to platform path separator
    if (File.separatorChar != '/')
      {
        name = name.replace('/', File.separatorChar);
      }
    File f = new File(file, name);
    return new MboxFolder(store, f, false);
  }

  /**
   * Checks if the current file is or is supposed to be
   * compressed. Uses the filename to figure it out.
   */
  private boolean isGzip() 
  {
    return file.getName().toLowerCase().endsWith(".gz");
  }

  /**
   * Creates an input stream that possibly will decompress the
   * file contents.
   */
  private InputStream getInputStream() 
    throws IOException 
  {
    InputStream in = new FileInputStream(file);
    if (isGzip())
      {
        in = new GZIPInputStream(in);
      }
    return in;
  }

  /**
   * Creates an output stream that possibly will compress
   * whatever is sent to it, based on the current filename.
   */
  private OutputStream getOutputStream() 
    throws IOException 
  {
    OutputStream out = new FileOutputStream(file);
    if (isGzip())
      {
        out = new GZIPOutputStream(out);
      }
    return out;
  }
	
  /**
   * Locks this mailbox.
   * This uses a dotlock-like mechanism - see createNewFile().
   * If the directory containing the mbox
   * folder is not writable, we will not be able to open the mbox for
   * writing either.
   */
  public synchronized boolean acquireLock() 
  {
    MboxStore mstore = (MboxStore) store;
    try
      {
        String filename = file.getPath();
        String lockFilename = filename + ".lock";
        File lock = new File(lockFilename);
        mstore.log("creating " + lock.getPath());
        if (lock.exists())
          {
            return false;
          }
        //if (!lock.canWrite())
        //  return false;
        createNewFile(lock);
        return true;
      }
    catch (IOException e)
      {
        mstore.log("I/O exception acquiring lock on " + file.getPath());
      }
    catch (SecurityException e)
      {
        mstore.log("Security exception acquiring lock on " + file.getPath());
      }
    return false;
  }

  /**
   * This method creates a new file.
   * Because Java cannot properly dotlock a file by creating a temporary
   * file and hardlinking it(some platforms do not support hard links) we
   * must use this method to create a zero-length inode.
   * This is a replacement for File.createNewFile(), which only exists in
   * the JDK since 1.2.
   * The idea is simply to touch the specified file.
   */
  private void createNewFile(File file)
    throws IOException
  {
    // there may be another, more efficient way to do this.
    // certainly just setLastModified() does not work.
    BufferedOutputStream out =
      new BufferedOutputStream(new FileOutputStream(file));
    out.flush();
    out.close();
  }
	
  /**
   * Unlocks this mailbox.
   * This deletes any associated lockfile if it exists. It returns false if
   * an existing lockfile could not be deleted.
   */
  public synchronized boolean releaseLock() 
  {
    MboxStore mstore = (MboxStore) store;
    try
      {
        String filename = file.getPath();
        String lockFilename = filename + ".lock";
        File lock = new File(lockFilename);
        mstore.log("removing "+lock.getPath());
        if (lock.exists())
          {
            if (!lock.delete())
              {
                return false;
              }
          }
        return true;
      }
    catch (SecurityException e)
      {
        mstore.log("Security exception releasing lock on " + file.getPath());
      }
    return false;
  }

  class MboxFilenameFilter 
    implements FileFilter 
  {

    String pattern;
    int asteriskIndex, percentIndex;
	   
    MboxFilenameFilter(String pattern) 
    {
      this.pattern = pattern;
      asteriskIndex = pattern.indexOf('*');
      percentIndex = pattern.indexOf('%');
    }
	   
    public boolean accept(File f) 
    {
      String name = f.getName();
      if (asteriskIndex > -1) 
        {
          String start = pattern.substring(0, asteriskIndex);
          String end = pattern.substring(asteriskIndex + 1, pattern.length());
          return (name.startsWith(start) &&
                  name.endsWith(end));
        } 
      else if (percentIndex > -1) 
        {
          String start = pattern.substring(0, percentIndex);
          String end = pattern.substring(percentIndex + 1, pattern.length());
          File parent = f.getParentFile();
          return (parent != null && parent.equals(file) &&
                  name.startsWith(start) &&
                  name.endsWith(end));
        }
      return name.equals(pattern);
    }
  }
    
}
