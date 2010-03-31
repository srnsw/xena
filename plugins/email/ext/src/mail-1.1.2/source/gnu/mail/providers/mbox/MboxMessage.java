/*
 * MboxMessage.java
 * Copyright(C) 1999 Chris Burdess <dog@gnu.org>
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

import java.io.InputStream;
import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import gnu.mail.providers.ReadOnlyMessage;

/**
 * The message class implementing the Mbox mail protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class MboxMessage 
  extends ReadOnlyMessage 
{

  /**
   * Status header key.
   * This keeps the mbox flags.
   */
  protected static final String STATUS = "Status";
  
  /**
   * The From_ line associated with this message.
   * We will preserve this if possible.
   */
  protected String fromLine;

  /**
   * Creates a Mbox message.
   * This is called by the MboxStore.
   */
  protected MboxMessage(MboxFolder folder, 
                        String fromLine,
                        InputStream in,
                        int msgnum)
    throws MessagingException 
  {
    super(folder, in, msgnum);
    this.fromLine = fromLine;
    readStatusHeader();
  }

  /**
   * Creates a Mbox message.
   * This is called by the MboxFolder when appending.
   * It creates a copy of the specified message for the new folder.
   */
  protected MboxMessage(MboxFolder folder,
                        MimeMessage message,
                        int msgnum) 
    throws MessagingException 
  {
    super(message);
    this.folder = folder;
    this.msgnum = msgnum;
    readStatusHeader();
  }

  /**
   * Allow MboxFolder access to set the expunged flag after expunge.
   */
  protected void setExpunged(boolean expunged)
  {
    super.setExpunged(expunged);
  }
	
  /** 
   * Set the specified flags(reflected in the <code>Status</code> header).
   */
  public synchronized void setFlags(Flags flag, boolean set)
    throws MessagingException 
  {
    if (set)
      {
        flags.add(flag);
      }
    else
      {
        flags.remove(flag);
      }
  }
    
  /**
   * Updates the status header from the current flags.
   */
  protected void updateHeaders() 
    throws MessagingException 
  {
    super.updateHeaders();

    String old = getHeader(STATUS, "\n");
    StringBuffer buffer = new StringBuffer();
    boolean seen = flags.contains(Flags.Flag.SEEN);
    boolean recent = flags.contains(Flags.Flag.RECENT);
    boolean answered = flags.contains(Flags.Flag.ANSWERED);
    boolean deleted = flags.contains(Flags.Flag.DELETED);
    if (seen)
      {
        buffer.append('R');
      }
    if (!recent)
      {
        buffer.append('O');
      }
    if (answered)
      {
        buffer.append('A');
      }
    if (deleted)
      {
        buffer.append('D');
      }
    String status = buffer.toString();
    if (!(status.equals(old)))
      {
        setHeader(STATUS, status);
      }
  }


  /**
   * Reads the associated flags from the status header.
   */
  private void readStatusHeader() 
    throws MessagingException 
  {
    String[] currentStatus = this.getHeader(STATUS);
    if (currentStatus != null && currentStatus.length > 0) 
      {
        flags = new Flags();
        if (currentStatus[0].indexOf('R') >= 0)
          {
            flags.add(Flags.Flag.SEEN);
          }
        if (currentStatus[0].indexOf('O') < 0)
          {
            flags.add(Flags.Flag.RECENT);
          }
        if (currentStatus[0].indexOf('A') >= 0)
          {
            flags.add(Flags.Flag.ANSWERED);
          }
        if (currentStatus[0].indexOf('D') >= 0)
          {
            flags.add(Flags.Flag.DELETED);
          }
      }
  }

  // -- Utility methods --

  public boolean equals(Object other) 
  {
    if (other instanceof MimeMessage) 
      {
        MimeMessage message = (MimeMessage) other;
        return (message.getFolder() == getFolder() &&
                message.getMessageNumber() == getMessageNumber());
      }
    return false;
  }

}

