/*
 * NNTPTransport.java
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
import java.io.OutputStream;
import java.net.SocketException;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.TransportEvent;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import gnu.inet.nntp.NNTPConnection;

/**
 * An NNTP transport provider.
 * This uses an NNTPConnection to handle all the protocol-related
 * functionality.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 2.0
 */
public class NNTPTransport extends Transport
{

  NNTPConnection connection;

  /**
   * Constructor.
   * @param session the session
   * @param url the connection URL
   */
  public NNTPTransport(Session session, URLName url)
    {
      super(session, url);
    }

  /**
   * Performs the protocol connection.
   * @see NNTPStore#protocolConnect
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
      catch (SecurityException e)
        {
          if (username != null && password != null)
            {
              throw new AuthenticationFailedException(e.getMessage());
            }
          else
            {
              return false;
            }
        }
    }
  
  /**
   * Close the connection.
   * @see NNTPStore#close
   */
  public void close()
    throws MessagingException
    {
      try
        {
          synchronized (connection)
            {
              connection.quit();
            }
        }
      catch (IOException e)
        {
          if (!(e instanceof SocketException))
            {
              throw new MessagingException(e.getMessage(), e);
            }
        }
      super.close();
    }

  /**
   * Post an article.
   * @param message a MimeMessage
   * @param addresses an array of Address(ignored!)
   */
  public void sendMessage(Message message, Address[] addresses)
    throws MessagingException
    {
      // Ensure corrent recipient type, and that all newsgroup recipients are
      // of type NewsAddress.
      addresses = message.getRecipients(MimeMessage.RecipientType.NEWSGROUPS);
      boolean ok = (addresses.length > 0);
      if (!ok)
        {
          throw new MessagingException("No recipients specified");
        }
      for (int i = 0; i < addresses.length; i++)
        {
          if (!(addresses[i] instanceof NewsAddress))
            {
              ok = false;
              break;
            }
        }
      if (!ok)
        {
          throw new MessagingException("Newsgroup recipients must be "+
                                        "specified as type NewsAddress");
        }
      
      try
        {
          synchronized (connection)
            {
              OutputStream out = connection.post();
              message.writeTo(out);
              out.close();
            }
          notifyTransportListeners(TransportEvent.MESSAGE_DELIVERED,
                                    addresses,
                                    new NewsAddress[0],
                                    new NewsAddress[0],
                                    message);
        }
      catch (IOException e)
        {
          throw new MessagingException(e.getMessage(), e);
        }
    }
  
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
