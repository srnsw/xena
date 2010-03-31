/*
 * NNTPStore.java
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import gnu.inet.nntp.FileNewsrc;
import gnu.inet.nntp.Newsrc;
import gnu.inet.nntp.NNTPConnection;

/**
 * An NNTP store provider.
 * This uses an NNTPConnection to handle all the protocol-related
 * functionality.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 2.0
 */
public class NNTPStore extends Store
{

  static final Logger logger = Logger.getLogger("gnu.mail.providers.nntp");

  static final Level NNTP_TRACE = NNTPConnection.NNTP_TRACE;

  NNTPConnection connection;
  Newsrc newsrc;
  Folder root;

  /*
   * The permanent flags for NNTPFolders.
   */
  Flags permanentFlags;
  
  /**
   * Constructor.
   * @param session the session
   * @param url the connection URL
   */
  public NNTPStore(Session session, URLName url)
    {
      super(session, url);

      // The permanent flags for NNTPFolders.
      permanentFlags = new Flags();
      permanentFlags.add(Flags.Flag.RECENT);
      permanentFlags.add(Flags.Flag.SEEN);

      // Init newsrc
      String tn = getProperty("newsrc");
      if (tn != null)
        {
          try
            {
              ClassLoader l = Thread.currentThread().getContextClassLoader();
              Class t = l.loadClass(tn);
              newsrc = (Newsrc) t.newInstance();
            }
          catch (Exception e)
            {
              logger.log(NNTP_TRACE, "ERROR: unable to instantiate newsrc", e);
            }
        }
      else
        {
          File file = null;
          String filename = getProperty("newsrc.file");
          if (filename == null)
            {
              String home = System.getProperty("user.home");
              // ${HOME}/.newsrc[-${hostname}]
              String baseFilename = ".newsrc";
              StringBuffer buffer = new StringBuffer(baseFilename);
              if (url != null)
                {
                  buffer.append('-');
                  buffer.append(url.getHost());
                }
              file = new File(home, buffer.toString());
              if (!file.exists())
                {
                  // ${HOME}/.newsrc
                  file = new File(home, baseFilename);
                }
            }
          else
            {
              file = new File(filename);
            }
          newsrc = new FileNewsrc(file, session.getDebug());
        }
    }

  /**
   * Performs the protocol connection.
   */
  protected boolean protocolConnect(String host, int port, String username,
                                    String password)
    throws MessagingException
    {
      if (connection != null)
        {
          return true;
        }
      if (host == null)
        {
          host = getProperty("host");
        }
      if (username == null)
        {
          username = getProperty("user");
        }
      if (port < 0)
        {
          port = getIntProperty("port");
        }
      if (host == null)
        {
          return false;
        }
      try
        {
          int connectionTimeout = getIntProperty("connectiontimeout");
          int timeout = getIntProperty("timeout");
          if (port < 0)
            {
              port = NNTPConnection.DEFAULT_PORT;
            }
          if (session.getDebug())
            {
              NNTPConnection.logger.setLevel(NNTPConnection.NNTP_TRACE);
            }
          connection = new NNTPConnection(host, port,
                                          connectionTimeout, timeout);
          if (username != null && password != null)
            {
              // TODO decide on authentication method
              // Original authinfo
              return connection.authinfo(username, password);
            }
          else
            {
              return true;
            }
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
    }

  /**
   * Close the connection.
   */
  public void close()
    throws MessagingException
    {
      try
        {
          newsrc.close();
          synchronized (connection)
            {
              connection.quit();
            }
          }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
      super.close();
    }

  /**
   * Returns the folder representing the &quot;root&quot; namespace.
   * This folder can be used to browse the folder hierarchy.
   */
  public Folder getDefaultFolder()
    throws MessagingException
    {
      if (root == null)
        {
          root = new NNTPRootFolder(this);
        }
      return root;
    }

  /**
   * Returns a folder by name.
   */
  public Folder getFolder(String name)
    throws MessagingException
    {
      return getDefaultFolder().getFolder(name);
    }

  /**
   * Returns the folder whose name corresponds to the <code>file</code> part
   * of the specified URL.
   */
  public Folder getFolder(URLName url)
    throws MessagingException
    {
      return getDefaultFolder().getFolder(url.getFile());
    }

  /*
   * Indicates whether we should attempt to list all newsgroups.
   * There are >30,000 newsgroups on Usenet. A naive client is unlikely to
   * expect upwards of 30,000 folders to be returned from list().
   */
  boolean isListAll()
    {
      return propertyIsTrue("listall");
    }

  // -- Utility methods --

  private int getIntProperty(String key)
    {
      String value = getProperty(key);
      if (value != null)
        {
          try
            {
              return Integer.parseInt(value);
            }
          catch (RuntimeException e)
            {
            }
        }
      return -1;
    }

  private boolean propertyIsFalse(String key)
    {
      return "false".equals(getProperty(key));
    }

  private boolean propertyIsTrue(String key)
    {
      return "true".equals(getProperty(key));
    }

  /*
   * Returns the provider-specific or general mail property corresponding to
   * the specified key.
   */
  private String getProperty(String key)
    {
      String value = session.getProperty("mail.nntp." + key);
      if (value == null)
        {
          value = session.getProperty("mail." + key);
        }
      return value;
    }

}
