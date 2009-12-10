/*
 * MaildirMessage.java
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import gnu.mail.providers.ReadOnlyMessage;

/**
 * The message class implementing the Maildir mail protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class MaildirMessage 
  extends ReadOnlyMessage 
{

  /**
   * The "passed" info flag.
   */
  static final String PASSED = "Passed";

  /**
   * The file this message is stored in.
   */
  File file;

  /**
   * The unique name of this message.
   */
  String uniq;

  /**
   * Creates a Maildir message.
   * This is called by the MaildirFolder.
   */
  MaildirMessage(MaildirFolder folder, 
      File file,
      String uniq,
      String info,
      int msgnum)
    throws MessagingException 
  {
    super(folder, msgnum);
    this.file = file;
    this.uniq = uniq;
    // update flags
    if (info!=null && info.startsWith("2,"))
    {
      int len = info.length();
      for (int i=2; i<len; i++)
      {
        switch (info.charAt(i))
        {
          case 'D': // "draft"
            flags.add(Flags.Flag.DRAFT);
            break;
          case 'F': // "flagged"
            flags.add(Flags.Flag.FLAGGED);
            break;
          case 'P': // "passed"
            flags.add(PASSED);
            break;
          case 'R': // "replied"
            flags.add(Flags.Flag.ANSWERED);
            break;
          case 'S': // "seen"
            flags.add(Flags.Flag.SEEN);
            break;
          case 'T': // "trashed"
            flags.add(Flags.Flag.DELETED);
            break;
        }
      }
    }
  }

  /**
   * Creates a Maildir message.
   * This is called by the MaildirFolder when appending.
   * It creates a copy of the specified message for the new folder.
   */
  MaildirMessage(MaildirFolder folder,
      MimeMessage message,
      int msgnum) 
    throws MessagingException 
  {
    super(message);
    this.folder = folder;
    this.msgnum = msgnum;
  }

  /**
   * Allow MaildirFolder access to set the expunged flag after expunge.
   */
  protected void setExpunged(boolean expunged)
  {
    super.setExpunged(expunged);
  }
	
  /** 
   * Set the specified flags(reflected in the <code>info</code> field).
   */
  public synchronized void setFlags(Flags flag, boolean set)
    throws MessagingException 
  {
    if (set)
      flags.add(flag);
    else
      flags.remove(flag);
    if (flag.contains(Flags.Flag.SEEN))
     ((MaildirFolder)folder).setSeen(this, set);
  }

  /**
   * Returns the unique name of this message.
   */
  String getUniq()
  {
    return uniq;
  }
    
  /**
   * Returns an <code>info</code> field based on the current flags.
   */
  String getInfo() 
    throws MessagingException 
  {
    return getInfo(flags);
  }

  static String getInfo(Flags flags)
    throws MessagingException
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append('2');
    buffer.append(',');
    // Flags must be stored in ASCII order
    if (flags.contains(Flags.Flag.DRAFT))
      buffer.append('D'); // "draft" flag
    if (flags.contains(Flags.Flag.FLAGGED))
      buffer.append('F'); // "flagged" flag
    if (flags.contains(PASSED))
      buffer.append('P'); // "passed" flag
    if (flags.contains(Flags.Flag.ANSWERED))
      buffer.append('R'); // "replied" flag
    if (flags.contains(Flags.Flag.SEEN))
      buffer.append('S'); // "seen" flag
    if (flags.contains(Flags.Flag.DELETED))
      buffer.append('T'); // "trashed" flag
    return buffer.toString();
  }

  /**
   * Reads the message headers from the underlying file.
   */
  void fetchHeaders()
    throws MessagingException
  {
    if (headers!=null)
      return;
    try
    {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      headers = createInternetHeaders(in);
      in.close();
    }
    catch (IOException e)
    {
      throw new MessagingException(e.getMessage(), e);
    }
  }

  /**
   * Reads the entire message from the underlying file.
   */
  void fetch()
    throws MessagingException
  {
    if (content!=null)
      return;
    try
    {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      parse(in);
      in.close();
    }
    catch (IOException e)
    {
      throw new MessagingException(e.getMessage(), e);
    }
  }

  // -- Headers --

  public String[] getHeader(String name)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getHeader(name);
  }

  public String getHeader(String name, String delimiter)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getHeader(name, delimiter);
  }

  public Enumeration getAllHeaders()
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getAllHeaders();
  }
  
  public Enumeration getAllHeaderLines()
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getAllHeaderLines();
  }
  
  public Enumeration getMatchingHeaders(String[] names)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getMatchingHeaders(names);
  }
  
  public Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getMatchingHeaderLines(names);
  }
  
  public Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getNonMatchingHeaders(names);
  }

  public Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException
  {
    if (headers==null)
      fetchHeaders();
    return super.getNonMatchingHeaderLines(names);
  }

  // -- Content --

  public DataHandler getDataHandler()
    throws MessagingException
  {
    if (content==null)
      fetch();
    return super.getDataHandler();
  }

  protected InputStream getContentStream()
    throws MessagingException
  {
    if (content==null)
      fetch();
    return super.getContentStream();
  }

  // -- Utility methods --

  public boolean equals(Object other) 
  {
    if (other instanceof MimeMessage) 
    {
      MimeMessage message = (MimeMessage)other;
      return (message.getFolder()==getFolder() &&
          message.getMessageNumber()==getMessageNumber());
    }
    return false;
  }

}
