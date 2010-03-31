/*
 * $Id$
 * Copyright(C) 2004 The Free Software Foundation
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

package gnu.mail.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;

/**
 * A URLConnection that can be used to access mailboxes using the JavaMail
 * API.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version $Revision$ $Date$
 */
public class MailboxURLConnection
extends URLConnection
{

  /**
   * The mail store.
   */
  protected Store store;

  /**
   * The mail folder.
   */
  protected Folder folder;

  /**
   * The mail message, if the URL represents a message.
   */
  protected Message message;

  /**
   * The headers to return.
   */
  protected Map headers;
  private List headerKeys;

  /**
   * Constructs a new mailbox URL connection using the specified URL.
   * @param url the URL representing the mailbox to connect to
   */
  public MailboxURLConnection(URL url)
  {
    super(url);
  }

  /**
   * Connects to the mailbox.
   */
  public synchronized void connect()
    throws IOException
  {
    if (connected)
      return;

    try
    {
      Session session = Session.getDefaultInstance(System.getProperties());
      URLName urlName = asURLName(url);
      store = session.getStore(urlName);
      folder = store.getDefaultFolder();

      // Resolve to a folder
      String path = url.getPath();
      if ("/".equals(path))
        folder = folder.getFolder("INBOX");
      else
      {
        if (path.charAt(0) == '/')
          path = path.substring(1);
        int si = path.indexOf('/');
        while (si != -1 && path.length() > 0)
        {
          String comp = path.substring(0, si);
          path = path.substring(si + 1);
          
          folder = folder.getFolder(comp);
        }
      }

      if (!folder.exists())
        throw new FileNotFoundException(path);
      
      folder.open(Folder.READ_ONLY);
      
      // Find message if requested
      String ref = url.getRef();
      if (ref != null)
      {
        SearchTerm term = new HeaderTerm("Message-Id", ref);
        Message[] messages = folder.search(term);
        if (messages.length > 0)
          message = messages[0];
        else
          throw new FileNotFoundException(ref);
        
        headers = new HashMap();
        headerKeys = new ArrayList();

        if (message instanceof MimeMessage)
        {
          MimeMessage mm = (MimeMessage)message;
          Enumeration e = mm.getAllHeaderLines();
          while (e.hasMoreElements())
          {
            Header header = (Header)e.nextElement();
            headerKeys.add(header.getName());
            headers.put(header.getName(), header.getValue());
          }
        }
        else
        {
        }
      }
      else
      {
        headers = Collections.EMPTY_MAP;
        headerKeys = Collections.EMPTY_LIST;
      }
    }
    catch (MessagingException e)
    {
      Exception e2 = e.getNextException();
      if (e2 instanceof IOException)
        throw (IOException)e2;
      throw new IOException(e.getMessage());
    }
    connected = true;
  }

  public String getHeaderField(int index)
  {
    return getHeaderField(getHeaderFieldKey(index));
  }

  public String getHeaderFieldKey(int index)
  {
    return (String)headerKeys.get(index);
  }

  public String getHeaderField(String name)
  {
    return (String)headers.get(name);
  }

  public Map getHeaderFields()
  {
    return Collections.unmodifiableMap(headers);
  }

  public Object getContent()
    throws IOException
  {
    if (message != null)
      return message;
    else
      return folder;
  }

  public InputStream getInputStream()
    throws IOException
  {
    PipedOutputStream pos = new PipedOutputStream();
    Runnable writer;
    if (message == null)
      writer = new FolderWriter(folder, pos);
    else
      writer = new MessageWriter(message, pos);
    Thread thread = new Thread(writer,
        "MailboxURLConnection.getInputStream");
    thread.start();
    return new PipedInputStream(pos);
  }

  /**
   * Converts a URL into a URLName.
   */
  protected static URLName asURLName(URL url)
  {
    String protocol = url.getProtocol();
    String host = url.getHost();
    int port = url.getPort();
    String userInfo = url.getUserInfo();
    String username = null;
    String password = null;
    String path = url.getPath();

    if (userInfo != null)
    {
      int ci = userInfo.indexOf(':');
      username = (ci != -1) ? userInfo.substring(0, ci) : userInfo;
      password = (ci != -1) ? userInfo.substring(ci + 1) : null;
    }

    return new URLName(protocol, host, port, path, username, password);
  }

  static class MessageWriter
    implements Runnable
  {

    Message message;
    OutputStream out;

    MessageWriter(Message message, OutputStream out)
    {
      this.message = message;
      this.out = out;
    }

    public void run()
    {
      try
      {
        if (message instanceof MimeMessage)
         ((MimeMessage)message).writeTo(out);
        else
        {
          // TODO
        }
      }
      catch (Exception e)
      {
        // ?
      }
    }
    
  }
  
  static class FolderWriter
    implements Runnable
  {

    Folder folder;
    OutputStream out;

    FolderWriter(Folder folder, OutputStream out)
    {
      this.folder = folder;
      this.out = out;
    }

    public void run()
    {
      // TODO
    }
    
  }
  
}
