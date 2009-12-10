/*
 * IMAPBodyPart.java
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

package gnu.mail.providers.imap;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;

import gnu.inet.imap.IMAPConnection;
import gnu.inet.imap.IMAPConstants;
import gnu.inet.imap.MessageStatus;
import gnu.inet.imap.Pair;

/**
 * A MIME body part of an IMAP multipart message.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class IMAPBodyPart
extends MimeBodyPart
implements IMAPConstants
{

  /**
   * The message this part belongs to.
   */
  protected IMAPMessage message;

  /**
   * The section used to refer to this part.
   */
  protected String section;

  /**
   * The size of this part's content in bytes.
   */
  protected int size;

  /**
   * The number of text lines of this part's content.
   */
  protected int lines;

  /*
   * Multipart content.
   */
  IMAPMultipart multipart = null;
  
  /**
   * Called by the IMAPMessage.
   */
  protected IMAPBodyPart(IMAPMessage message,
                          IMAPMultipart parent,
                          String section,
                          InternetHeaders headers,
                          int size,
                          int lines)
    throws MessagingException
  {
    super(headers, null);
    this.parent = parent;
    this.message = message;
    this.section = section;
    this.size = size;
    this.lines = lines;
  }

  /**
   * Fetches the message body.
   */
  void fetchContent()
    throws MessagingException
  {
    String[] commands = new String[1];
    commands[0] = "BODY.PEEK[" + section + "]";
    fetch(commands);
  }

  /*
   * Perform the IMAP fetch.
   */
  void fetch(String[] commands)
    throws MessagingException
  {
    try
      {
        IMAPConnection connection =
         ((IMAPStore) message.getFolder().getStore()).getConnection();
        int msgnum = message.getMessageNumber();
        int[] messages = new int[] { msgnum };
        synchronized (connection)
          {
            MessageStatus[] ms = connection.fetch(messages, commands);
            for (int i = 0; i < ms.length; i++)
              {
                if (ms[i].getMessageNumber() == msgnum)
                  {
                    update(ms[i]);
                  }
              }
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /*
   * Update this body part's content from the specified message status
   * object.
   */
  void update(MessageStatus status)
    throws MessagingException
  {
    List code = status.getCode();
    int clen = code.size();
    for (int i = 0; i < clen; i += 2)
      {
        Object item = code.get(i);
        String key = null;
        List params = Collections.EMPTY_LIST;
        if (item instanceof Pair)
          {
            Pair pair = (Pair) item;
            key = pair.getKey();
            params = pair.getValue();
          }
        else if (item instanceof String)
          {
            key = (String) item;
          }
        else
          {
            throw new MessagingException("Unexpected status item: " + item);
          }
        
        if (key == BODY)
          {
            int plen = params.size();
            if (plen > 0)
              {
                Object pitem = params.get(0);
                String pkey = null;
                if (pitem instanceof String)
                  {
                    pkey = (String) pitem;
                  }
                else
                  {
                    throw new MessagingException("Unexpected status item: " +
                                                  pitem);
                  }
                
                if (pkey.equals(section))
                  {
                    Object c = code.get(i + 1);
                    if (c instanceof byte[])
                      content = (byte[]) c;
                    else if (c instanceof String)
                      {
                        ContentType ct = new ContentType(getContentType());
                        String charset = ct.getParameter("charset");
                        if (charset == null)
                          charset = "US-ASCII";
                        try
                          {
                            content = ((String) c).getBytes(charset);
                          }
                        catch (IOException e)
                          {
                            MessagingException e2 = new MessagingException();
                            e2.initCause(e);
                            throw e2;
                          }
                      }
                    else
                      throw new MessagingException("Unexpected MIME body " +
                                                   "part content: " + c);
                  }
                else
                  {
                    throw new MessagingException("Unexpected section number: " +
                                                  pkey);
                  }
              }
            else
              {
                throw new MessagingException("Not a section!");
              }
          }
        else
          {
            throw new MessagingException("Unknown section status key: " + key);
          }
      }
  }

  // -- Simple accessors --

  /**
   * Returns the content size of this body part in bytes.
   */
  public int getSize()
    throws MessagingException
  {
    return size;
  }

  /**
   * Returns the number of text lines in the content of this body part.
   */
  public int getLineCount()
    throws MessagingException
  {
    return lines;
  }

  // -- Content access --

  /**
   * Returns a data handler for this part's content.
   */
  public DataHandler getDataHandler()
    throws MessagingException
  {
    ContentType ct = new ContentType(getContentType());
    if ("multipart".equalsIgnoreCase(ct.getPrimaryType()))
      {
        // Our multipart object should already have been configured
        return new DataHandler(new IMAPMultipartDataSource(multipart));
      }
    else
      {
        if (content == null)
          {
            fetchContent();
          }
        return super.getDataHandler();
      }
  }

  public Object getContent()
    throws MessagingException, IOException
  {
    ContentType ct = new ContentType(getContentType());
    if ("multipart".equalsIgnoreCase(ct.getPrimaryType()))
      {
        return multipart;
      }
    return super.getContent();
  }
  
  /**
   * Returns the raw content stream.
   */
  protected InputStream getContentStream()
    throws MessagingException
  {
    if (content == null)
      {
        fetchContent();
      }
    return super.getContentStream();
  }

}
