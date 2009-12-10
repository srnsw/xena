/*
 * NNTPFolder.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.event.ConnectionEvent;

import gnu.inet.nntp.ArticleResponse;
import gnu.inet.nntp.GroupResponse;
import gnu.inet.nntp.HeaderEntry;
import gnu.inet.nntp.HeaderIterator;
import gnu.inet.nntp.NNTPConstants;
import gnu.inet.nntp.NNTPException;

/**
 * A JavaMail folder delegate for an NNTP newsgroup.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 2.0
 */
public final class NNTPFolder
  extends Folder
{

  String name;
  int first = -1;
  int last = -1;
  int count = -1;
  boolean open;

  Map articleCache; // cache of article-number to NNTPMessage
  
  NNTPFolder(NNTPStore store, String name)
    {
      super(store);
      this.name = name;
    }

  /**
   * Returns the name of the newsgroup, e.g. <code>alt.test</code>.
   */
  public String getName()
    {
      return name;
    }

  /**
   * @see #getName
   */
  public String getFullName()
    {
      return name;
    }

  /**
   * This implementation uses a flat namespace, so the parent of any
   * NNTPFolder is the NNTP root folder.
   */
  public Folder getParent()
    throws MessagingException
    {
      NNTPStore ns = (NNTPStore) store;
      return ns.root;
    }
  
  /**
   * Returns the type of this folder.
   * This folder type only holds messages.
   */
  public int getType()
    throws MessagingException
    {
      return HOLDS_MESSAGES;
    }

  public boolean isOpen()
    {
      return open;
    }

  /**
   * This folder type is always read-only.
   */
  public int getMode()
    {
      return READ_ONLY;
    }

  /**
   * Returns the flags supported by this folder.
   */
  public Flags getPermanentFlags()
    {
      NNTPStore ns = (NNTPStore) store;
      return new Flags(ns.permanentFlags);
    }

  /**
   * This method has no particular meaning in NNTP.
   * However, we will use it to send a GROUP command and refresh our article
   * stats.
   */
  public void open(int mode)
    throws MessagingException
    {
      // NB this should probably throw an exception if READ_WRITE is
      // specified, but this tends to cause problems with existing clients.
      if (open)
        {
          throw new IllegalStateException();
        }
      try
        {
          NNTPStore ns = (NNTPStore) store;
          synchronized (ns.connection)
            {
              GroupResponse response = ns.connection.group(name);
              count = response.count;
              first = response.first;
              last = response.last;
            }

          articleCache = new HashMap(1024); // TODO make configurable
          open = true;
          notifyConnectionListeners(ConnectionEvent.OPENED);
        }
      catch (NNTPException e)
        {
          if (e.getResponse().getStatus() == NNTPConstants.NO_SUCH_GROUP)
            {
              throw new FolderNotFoundException(e.getMessage(), this);
            }
          else
            {
              throw new MessagingException(e.getMessage(), e);
            }
        }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
    }

  /**
   * This method has no particular meaning in NNTP.
   */
  public void close(boolean expunge)
    throws MessagingException
    {
      if (!open)
        {
          throw new IllegalStateException();
        }
      
      articleCache = null;
      open = false;
      notifyConnectionListeners(ConnectionEvent.CLOSED);
    }
  
  /**
   * Indicates whether the newsgroup is present on the server.
   */
  public boolean exists()
    throws MessagingException
    {
      try
        {
          NNTPStore ns = (NNTPStore) store;
          synchronized (ns.connection)
            {
              GroupResponse response = ns.connection.group(name);
              count = response.count;
              first = response.first;
              last = response.last;
            }
          return true;
        }
      catch (NNTPException e)
        {
          if (e.getResponse().getStatus() == NNTPConstants.NO_SUCH_GROUP)
            {
              return false;
            }
          else
            {
              throw new MessagingException(e.getMessage(), e);
            }
        }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
    }

  /**
   * Indicates whether there are new articles in this newsgroup.
   */
  public boolean hasNewMessages()
    throws MessagingException
    {
      try
        {
          NNTPStore ns = (NNTPStore) store;
          boolean hasNew = false;
          synchronized (ns.connection)
            {
              GroupResponse response = ns.connection.group(name);
              if (response.last > last)
                {
                  hasNew = true;
                }
              count = response.count;
              first = response.first;
              last = response.last;
            }
          return hasNew;
        }
      catch (NNTPException e)
        {
          if (e.getResponse().getStatus() == NNTPConstants.NO_SUCH_GROUP)
            {
              throw new FolderNotFoundException(e.getMessage(), this);
            }
          else
            {
              throw new MessagingException(e.getMessage(), e);
            }
        }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
    }
  
  /**
   * Returns the number of articles in this newsgroup.
   */
  public int getMessageCount()
    throws MessagingException
    {
      return count;
    }

  /**
   * Returns the article corresponding to the specified article
   * number.
   * @throws MessageRemovedException often ;-)
   */
  public Message getMessage(int msgnum)
    throws MessagingException
    {
      if (!open)
        {
          throw new IllegalStateException();
        }
      
      // Cache lookup
      Integer key = new Integer(msgnum);
      NNTPMessage m = (NNTPMessage) articleCache.get(key);
      if (m!=null)
        {
          return m;
        }
      
      try
        {
          NNTPStore ns = (NNTPStore) store;
          synchronized (ns.connection)
            {
              // Ensure group selected
              GroupResponse gr = ns.connection.group(name);
              first = gr.first;
              last = gr.last;
              count = gr.count;
              // Get article
              m = getMessageImpl(msgnum - 1 + first);
              // Cache store
              articleCache.put(key, m);
              return m;
            }
        }
      catch (NNTPException e)
          {
            switch (e.getResponse().getStatus())
              {
              case NNTPConstants.NO_ARTICLE_SELECTED:
              case NNTPConstants.NO_SUCH_ARTICLE_NUMBER:
              case NNTPConstants.NO_SUCH_ARTICLE:
                throw new MessageRemovedException(e.getMessage());
              default:
                throw new MessagingException(e.getMessage(), e);
              }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }

  /*
   * Perform article STAT.
   * NB not synchronized against the connection!
   */
  NNTPMessage getMessageImpl(int msgnum)
    throws IOException
    {
      NNTPStore ns = (NNTPStore) store;
      // Issue STAT
      ArticleResponse response = ns.connection.stat(msgnum);
      String messageId = response.messageId;
      return new NNTPMessage(this, msgnum, messageId);
    }

  /**
   * Returns all articles in this group.
   * This tries XHDR first to retrieve Message-IDs for the articles.
   * If this fails we fall back to statting each article.
   */
  public Message[] getMessages()
    throws MessagingException
    {
      NNTPStore ns = (NNTPStore) store;
      List acc = new LinkedList();
      synchronized (ns.connection)
        {
          try
            {
              // Ensure group selected
              GroupResponse gr = ns.connection.group(name);
              first = gr.first;
              last = gr.last;
              count = gr.count;
              // Get Message-IDs for all article numbers
              StringBuffer rb = new StringBuffer();
              rb.append(Integer.toString(first));
              rb.append('-');
              rb.append(Integer.toString(last));
              HeaderIterator i = ns.connection.xhdr("Message-ID",
                                                     rb.toString());
              while (i.hasNext())
                {
                  HeaderEntry entry = i.nextHeaderEntry();
                  Integer key = new Integer(entry.getArticleId());
                  // Cache lookup
                  NNTPMessage m = (NNTPMessage) articleCache.get(key);
                  if (m == null)
                    {
                      int msgnum = key.intValue();
                      String messageId = entry.getHeader();
                      m = new NNTPMessage(this, msgnum, messageId);
                      // Cache store
                      articleCache.put(key, m);
                    }
                  acc.add(m);
                }
            }
          catch (NNTPException e)
            {
              // Perhaps the server does not understand XHDR.
              // We'll do it the slow way.
              for (int i = first; i <= last; i++)
                {
                  Integer key = new Integer(i);
                  // Cache lookup
                  Message m = (NNTPMessage) articleCache.get(key);
                  if (m == null)
                    {
                      try 
                        {
                          m = getMessageImpl(i);
                          // Cache store
                          articleCache.put(key, m);
                          acc.add(m);
                        }
                      catch (NNTPException e2)
                        {
                          switch (e2.getResponse().getStatus())
                            {
                            case NNTPConstants.NO_ARTICLE_SELECTED:
                            case NNTPConstants.NO_SUCH_ARTICLE_NUMBER:
                            case NNTPConstants.NO_SUCH_ARTICLE:
                              break; // article does not exist, ignore
                            default:
                              throw new MessagingException(e2.getMessage(),
                                                            e2);
                            }
                        }
                      catch (IOException ie)
                        {
                          throw new MessagingException(ie.getMessage(), ie);
                        }
                    }
                }
            }
          catch (IOException e)
            {
              throw new MessagingException(e.getMessage(), e);
            }
        }
      int len = acc.size();
      Message[] messages = new Message[len];
      acc.toArray(messages);
      return messages;
    }

  /**
   * Prefetch.
   */
  public void fetch(Message[] msgs, FetchProfile fp)
    throws MessagingException
    {
      boolean head = fp.contains(FetchProfile.Item.ENVELOPE);
      head = head ||(fp.getHeaderNames().length > 0);
      boolean body = fp.contains(FetchProfile.Item.CONTENT_INFO);
      int op = (head && body) ? 3 : head ? 2 : body ? 1 : 0;
      try
        {
          NNTPStore ns = (NNTPStore) store;
          for (int i = 0; i < msgs.length; i++)
            {
              Message msg = msgs[i];
              if (msg == null || !(msg instanceof NNTPMessage))
                {
                  continue;
                }
              NNTPMessage message = (NNTPMessage) msg;
              String messageId = message.getMessageId();

              ArticleResponse response = null;
              synchronized (ns.connection)
                {
                  switch (op)
                    {
                    case 3: // head & body
                      response = ns.connection.article(messageId);
                      break;
                    case 2: // head
                      response = ns.connection.head(messageId);
                      break;
                    case 1: // body
                      response = ns.connection.body(messageId);
                      break;
                    }
                  ByteArrayOutputStream out = new ByteArrayOutputStream();
                  byte[] buf = new byte[4096];
                  for (int len = response.in.read(buf);
                       len >- 1;
                       len = response.in.read(buf))
                    {
                      out.write(buf, 0, len);
                    }
                  switch (op)
                    {
                    case 3: // head & body
                      ByteArrayInputStream hbin =
                        new ByteArrayInputStream(out.toByteArray());
                      message.updateHeaders(hbin);
                      int len = hbin.available();
                      byte[] content = new byte[len];
                      hbin.read(content);
                      message.updateContent(content);
                      break;
                    case 2: // head
                      ByteArrayInputStream hin =
                        new ByteArrayInputStream(out.toByteArray());
                      message.updateHeaders(hin);
                      break;
                    case 1: // body
                      message.updateContent(out.toByteArray());
                      break;
                    }
                }
            }
        }
      catch (NNTPException e)
        {
          switch (e.getResponse().getStatus())
            {
            case NNTPConstants.NO_GROUP_SELECTED:
              throw new IllegalStateException(e.getMessage());
            case NNTPConstants.NO_ARTICLE_SELECTED:
            case NNTPConstants.NO_SUCH_ARTICLE_NUMBER:
            case NNTPConstants.NO_SUCH_ARTICLE:
              throw new MessageRemovedException(e.getMessage());
            default:
              throw new MessagingException(e.getMessage(), e);
            }
        }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
    }

  // -- Subscription --

  /**
   * Indicates if the newsgroup is subscribed.
   * This uses the newsrc mechanism associated with this folder's store.
   */
  public boolean isSubscribed()
    {
      NNTPStore ns = (NNTPStore) store;
      return ns.newsrc.isSubscribed(name);
    }

  /**
   * Subscribes or unsubscribes to this newsgroup.
   * This uses the newsrc mechanism associated with this folder's store.
   */
  public void setSubscribed(boolean flag)
    throws MessagingException
    {
      NNTPStore ns = (NNTPStore) store;
      ns.newsrc.setSubscribed(name, flag);
    }

  boolean isSeen(int articleNumber)
    {
      NNTPStore ns = (NNTPStore) store;
      return ns.newsrc.isSeen(name, articleNumber);
    }

  void setSeen(int articleNumber, boolean flag)
    {
      NNTPStore ns = (NNTPStore) store;
      ns.newsrc.setSeen(name, articleNumber, flag);
    }

  // -- Stuff we can't do --

  /**
   * This folder type does not contain other folders.
   */
  public Folder getFolder(String name)
    throws MessagingException
    {
      throw new MethodNotSupportedException();
    }

  /**
   * This folder type does not contain other folders.
   */
  public Folder[] list(String pattern)
    throws MessagingException
    {
      throw new MethodNotSupportedException();
    }

  /**
   * This folder type does not contain other folders.
   */
  public Folder[] listSubscribed(String pattern)
    throws MessagingException
    {
      return list(pattern);
    }

  /**
   * If we move away from a flat namespace, this might be useful.
   */
  public char getSeparator()
    throws MessagingException
    {
      return '.';
    }

  /**
   * NNTP servers are read-only.
   */
  public boolean create(int type)
    throws MessagingException
    {
      throw new MethodNotSupportedException();
    }

  /**
   * NNTP servers are read-only.
   */
  public boolean delete(boolean recurse)
    throws MessagingException
    {
      throw new MethodNotSupportedException();
    }

  /**
   * NNTP servers are read-only.
   */
  public boolean renameTo(Folder folder)
    throws MessagingException
    {
      throw new MethodNotSupportedException();
    }

  /**
   * NNTP servers are read-only.
   */
  public void appendMessages(Message[] messages)
    throws MessagingException
    {
      throw new IllegalWriteException();
    }

  /**
   * NNTP servers are read-only.
   */
  public Message[] expunge()
    throws MessagingException
    {
      throw new IllegalWriteException();
    }

}
