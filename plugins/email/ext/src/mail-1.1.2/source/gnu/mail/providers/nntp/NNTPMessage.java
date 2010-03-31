/*
 * NNTPMessage.java
 * Copyright(C) 2002 Chris Burdess <dog@gnu.org>
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

package gnu.mail.providers.nntp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;

/**
 * A JavaMail MIME message delegate for an NNTP article.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 2.0
 */
public final class NNTPMessage
  extends MimeMessage
{

  String messageId;
  
  NNTPMessage(NNTPFolder folder,
              int msgnum,
              String messageId)
    {
      super(folder, msgnum);
      this.messageId = messageId;
      headers = null;
      // Set SEEN state
      flags = folder.getPermanentFlags();
      if (folder.isSeen(msgnum))
        {
          flags.add(Flags.Flag.SEEN);
        }
      else
        {
          flags.remove(Flags.Flag.SEEN);
        }
    }

  public String getMessageId()
    {
      return messageId;
    }

  void requestHeaders()
    throws MessagingException
    {
      FetchProfile fp = new FetchProfile();
      fp.add(FetchProfile.Item.ENVELOPE);
      NNTPMessage[] messages = new NNTPMessage[1];
      messages[0] = this;
      folder.fetch(messages, fp);
    }

  /*
   * Called by NNTPFolder
   */
  void updateHeaders(InputStream in)
    throws MessagingException, IOException
    {
      headers = new InternetHeaders(in);
    }

  void requestContent()
    throws MessagingException
    {
      FetchProfile fp = new FetchProfile();
      fp.add(FetchProfile.Item.CONTENT_INFO);
      NNTPMessage[] messages = new NNTPMessage[1];
      messages[0] = this;
      folder.fetch(messages, fp);
    }

  /*
   * Called by NNTPFolder
   */
  void updateContent(byte[] content)
    {
      this.content = content;
    }

  // -- Header retrieval --

  public String[] getHeader(String name)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getHeader(name);
    }

  public String getHeader(String name, String delimiter)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getHeader(name, delimiter);
    }

  public Enumeration getAllHeaders()
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getAllHeaders();
    }

  public Enumeration getMatchingHeaders(String[] names)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getMatchingHeaders(names);
    }

  public Enumeration getNonMatchingHeaders(String[] names)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getNonMatchingHeaders(names);
    }

  public Enumeration getAllHeaderLines()
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getAllHeaderLines();
    }

  public Enumeration getMatchingHeaderLines(String[] names)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getMatchingHeaderLines(names);
    }

  public Enumeration getNonMatchingHeaderLines(String[] names)
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      return super.getNonMatchingHeaderLines(names);
    }

  // setHeader / addHeader / removeHeader

  // -- Content retrieval --

  public int getSize()
    throws MessagingException
    {
      if (content == null)
        {
          requestContent();
        }
      return super.getSize();
    }

  public int getLineCount()
    throws MessagingException
    {
      String value = getHeader("Lines", ",");
      if (value != null)
        {
          try
            {
              return Integer.parseInt(value.trim());
            }
          catch (NumberFormatException e)
            {
            }
        }
      return -1;
    }

  public InputStream getContentStream()
    throws MessagingException
    {
      if (content == null)
        {
          requestContent();
        }
      return super.getContentStream();
    }

  // setContent(Object o, tring type), setContent(Multpart)

  public void saveChanges()
    throws MessagingException
    {
      if (headers == null)
        {
          requestHeaders();
        }
      if (content == null)
        {
          requestContent();
        }
    }

  // -- Update SEEN flag if necessary --

  public void setFlags(Flags flag, boolean set)
    throws MessagingException
    {
      if (flag.contains(Flags.Flag.SEEN))
        {
         ((NNTPFolder) folder).setSeen(msgnum, set);
        }
      super.setFlags(flag, set);
    }

}
