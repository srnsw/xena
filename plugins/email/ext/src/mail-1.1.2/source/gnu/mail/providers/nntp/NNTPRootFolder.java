/*
 * NNTPRootFolder.java
 * Copyright(C) 2002, 2006 Chris Burdess <dog@gnu.org>
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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessagingException;

import gnu.inet.nntp.Group;
import gnu.inet.nntp.GroupIterator;
import gnu.inet.nntp.NNTPConstants;
import gnu.inet.nntp.NNTPException;

/**
 * The &quot;root&quot; folder of the NNTP newsgroup list.
 * The NNTP folder namespace is taken to be a flat namespace.
 * This object allows us to retrieve folders corresponding to each newsgroup
 * in that space.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @author <a href='mailto:cedric.hyppolite@free.fr'>Cedric Hyppolite</a>
 * @version 2.0
 */
final public class NNTPRootFolder
  extends Folder
{

  NNTPRootFolder(NNTPStore store)
  {
    super(store);
  }

  public String getName()
  {
    NNTPStore ns = (NNTPStore) store;
    return ns.getURLName().getHost();
  }

  public String getFullName()
  {
    NNTPStore ns = (NNTPStore) store;
    return ns.connection.getWelcome();
  }
  
  /**
   * Returns the list of folders matching the specified pattern.
   * @param pattern the JavaMail pattern
   */
  public Folder[] list(String pattern) 
    throws MessagingException 
  {
    return list(pattern, null);
  }
  
  /**
   * Returns the list of folders matching the specified pattern.
   * @param listener the listener to be called as soon as a new folder is
   * listed
   */
  public Folder[] list(ListFolderListener listener) 
    throws MessagingException 
  {	     
    return list("%",  listener);
  }
  
  /**
   * Returns the list of folders matching the specified pattern.
   * @param pattern the JavaMail pattern
   * @param listener the listener that will be called for each folder name
   * as soon as it is known
   */
  public Folder[] list(String pattern, ListFolderListener listener)
    throws MessagingException
  {
    pattern = pattern.replace('%', '*'); // convert pattern to wildmat
    try
      {
        NNTPStore ns = (NNTPStore) store;
        // Indicates whether to *really* list all folders.
        boolean listAll = ns.isListAll();
        
        List acc = new LinkedList();
        synchronized (ns.connection)
          {
            GroupIterator i = listAll ?
              ns.connection.listActive(pattern) :
              ns.connection.listSubscriptions();
            while (i.hasNext())
              {
                Group group = i.nextGroup();
                NNTPFolder folder = new NNTPFolder(ns, group.getName());
                acc.add(folder);
                if (listener != null)
                  listener.foundFolder(group.getName());
              }
          }
        int len = acc.size();
        Folder[] folders = new Folder[len];
        acc.toArray(folders);
        return folders;
      }
    catch (NNTPException e)
      {
        switch (e.getResponse().getStatus())
          {
          case NNTPConstants.COMMAND_NOT_RECOGNIZED:
          case NNTPConstants.SYNTAX_ERROR:
          case NNTPConstants.INTERNAL_ERROR:
            return listSubscribed(pattern);
          default:
            throw new MessagingException(e.getMessage(), e);
          }
      }
    catch (IOException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Returns the list of subscribed folders matching the specified pattern.
   * @param pattern the JavaMail pattern
   */
  public Folder[] listSubscribed(String pattern)
    throws MessagingException
  {
    pattern = pattern.replace('%', '*'); // convert pattern to wildmat
    // Does the pattern contain any wildcards?
    boolean hasWildcard = pattern.indexOf('*') >- 1;
    // Does the pattern contain only a wildcard?
    boolean onlyWildcard = hasWildcard &&(pattern.length() == 0);
    
    NNTPStore ns = (NNTPStore) store;
    List acc = new LinkedList();
    Iterator i = ns.newsrc.list();
    while (i.hasNext())
      {
        String name = (String) i.next();
        // Check that name matches pattern
        if (!onlyWildcard)
          {
            if (hasWildcard && matches(name, pattern))
              acc.add(new NNTPFolder(ns, name));
            else if (!hasWildcard && pattern.equals(name))
              acc.add(new NNTPFolder(ns, name));
          }
      }
    int len = acc.size();
    Folder[] folders = new Folder[len];
    acc.toArray(folders);
    return folders;
  }
  
  /**
   * Implements a subset of wildmat matching on the client side.
   * This is necessary for newsgroup matching from newsrc lists.
   */
  boolean matches(String name, String pattern)
  {
    int i1 = pattern.indexOf('*');
    int pn = 0, pp = 0;
    while (i1 >- 1)
      {
        if (i1 > 0)
          {
            String ps = pattern.substring(pp, i1);
            int len = ps.length();
            String ns = name.substring(pn, len);
            if (!ps.equals(ns))
              {
                return false;
              }
            pp = i1 + 1;
            pn += len;
            i1 = 0;
          }
        else
          {
            pp = i1 + 1;
            i1 = pattern.indexOf('*', pp);
            String ps = null;
            if (i1 == -1)
              {
                ps = pattern.substring(pp);
              }
            else
              {
                ps = pattern.substring(pp, i1);
              }
            int len = ps.length();
            if (len > 0)
              {
                String ns = name.substring(pn, len);
                if (!ps.equals(ns))
                  {
                    return false;
                  }
              }
          }
      }
    return true;
  }
  
  /**
   * Returns a new Folder object associated with the specified name.
   */
  public Folder getFolder(String name)
    throws MessagingException
  {
    NNTPStore ns = (NNTPStore) store;
    return new NNTPFolder(ns, name);
  }

  public Folder getParent()
    throws MessagingException
  {
    return null;
  }
  
  public boolean exists()
    throws MessagingException
  {
    return true;
  }

  /**
   * As we're dealing with a flat namespace, the value of this is
   * irrelevant.
   */
  public char getSeparator()
    throws MessagingException
  {
    return '.';
  }

  /**
   * This folder contains only folders.
   */
  public int getType()
  {
    return HOLDS_FOLDERS;
  }
  
  public void open(int mode)
    throws MessagingException
  {
    // Although we will never actually _be_ open,
    // it's always good to remind people....
    if (mode != READ_ONLY)
      {
        throw new IllegalWriteException("Folder is read-only");
      }
  }

  public void close(boolean expunge)
    throws MessagingException
  {
  }

  public Message[] expunge()
    throws MessagingException
  {
    throw new IllegalWriteException("Folder is read-only");
  }

  public boolean isOpen()
  {
    return false;
  }
  
  public Flags getPermanentFlags()
  {
    return new Flags();
  }

  public int getMessageCount()
    throws MessagingException
  {
    return -1;
  }

  public Message getMessage(int msgnum)
    throws MessagingException
  {
    throw new IllegalStateException("Folder not open");
  }

  /**
   * This folder is always &quot;subscribed&quot;.
   */
  public void setSubscribed(boolean flag)
    throws MessagingException
  {
    if (!flag)
      {
        throw new IllegalWriteException("Can't unsubscribe root folder");
      }
  }

  public boolean hasNewMessages()
    throws MessagingException
  {
    return false;
  }

  public void appendMessages(Message[] messages)
    throws MessagingException
  {
    throw new IllegalWriteException("Folder is read-only");
  }
  
  public boolean create(int type)
    throws MessagingException
  {
    throw new MessagingException("Folder already exists");
  }
  
  public boolean delete(boolean flag)
    throws MessagingException
  {
    throw new IllegalWriteException("Folder is read-only");
  }

  public boolean renameTo(Folder folder)
    throws MessagingException
  {
    throw new IllegalWriteException("Folder is read-only");
  }

}
