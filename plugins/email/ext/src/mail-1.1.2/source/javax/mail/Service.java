/*
 * Service.java
 * Copyright (C) 2002, 2005 The Free Software Foundation
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

package javax.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;

/**
 * An abstract messaging service (store or transport).
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Service
{

  /**
   * The session context for this service.
   */
  protected Session session;

  /**
   * The URLName of this service.
   */
  protected URLName url;

  /**
   * The debug flag for this service.
   * Initialised from the session's debug flag when this service is created.
   */
  protected boolean debug;

  /*
   * @see #isConnected
   */
  private boolean connected = false;

  private ArrayList connectionListeners = null;

  /**
   * Constructor.
   * @param session the session context for this service
   * @param url the URLName of this service
   */
  protected Service(Session session, URLName url)
  {
    this.session = session;
    this.url = url;
    debug = session.getDebug();
  }

  /**
   * Connects to this service.
   * If additional information is required, the provider can determine them
   * from session properties or via a callback to the UI.
   * @exception AuthenticationFailedException on authentication failure
   * @exception MessagingException for other failures
   * @exception IllegalStateException if the service is already connected
   */
  public void connect()
    throws MessagingException
  {
    connect(null, null, null);
  }

  /**
   * Connects to this service.
   * This method provides a simple authentication scheme requiring a
   * username and password. The host is determined from the inital URLName.
   * @param user the username
   * @param password the password
   * @exception AuthenticationFailedException on authentication failure
   * @exception MessagingException for other failures
   * @exception IllegalStateException if the service is already connected
   * @since JavaMail 1.4
   */
  public void connect(String user, String password)
    throws MessagingException
  {
    connect(null, user, password);
  }

  /**
   * Connects to this service using the specified details.
   * This method provides a simple authentication scheme requiring a
   * username and password.
   * @param host the host to connect to
   * @param user the username
   * @param password the password
   * @exception AuthenticationFailedException on authentication failure
   * @exception MessagingException for other failures
   * @exception IllegalStateException if the service is already connected
   */
  public void connect(String host, String user, String password)
    throws MessagingException
  {
    connect(host, -1, user, password);
  }

  /**
   * Connects to this service using the specified details.
   * This method provides a simple authentication scheme requiring a
   * username and password.
   * @param host the host to connect to
   * @param port the port to use (-1 for the default port)
   * @param user the username
   * @param password the password
   * @exception AuthenticationFailedException on authentication failure
   * @exception MessagingException for other failures
   * @exception IllegalStateException if the service is already connected
   */
  public void connect(String host, int port, String user, String password)
    throws MessagingException
  {
    if (isConnected())
      {
        throw new MessagingException("already connected");
      }
    
    boolean success = false;
    boolean authenticated = false;
    String protocol = null;
    String file = null;
    if (url != null)
      {
        protocol = url.getProtocol();
        if (host == null)
          {
            host = url.getHost();
          }
        if (port == -1)
          {
            port = url.getPort();
          }
        if (user == null)
          {
            user = url.getUsername();
            if (password == null)
              {
                password = url.getPassword();
              }
          }
        else if (password == null && user.equals(url.getUsername()))
          {
            password = url.getPassword();
          }
        file = url.getFile();
      }
    if (protocol != null)
      {
        if (host == null)
          {
            host = session.getProperty("mail." + protocol + ".host");
          }
        if (user == null)
          {
            user = session.getProperty("mail." + protocol + ".user");
          }
      }
    if (host == null)
      {
        host = session.getProperty("mail.host");
      }
    if (user == null)
      {
        user = session.getProperty("mail.user");
      }
    if (user == null)
      {
        try
          {
            user = System.getProperty("user.name");
          }
        catch (SecurityException e)
          {
            if (debug)
              {
                e.printStackTrace();
              }
          }
      }
    if (password == null && url != null)
      {
        setURLName(new URLName(protocol, host, port, file, user, password));
        PasswordAuthentication auth = 
          session.getPasswordAuthentication(getURLName());
        if (auth != null)
          {
            if (user == null)
              {
                user = auth.getUserName();
                password = auth.getPassword();
              }
            else if (user.equals(auth.getUserName()))
              {
                password = auth.getPassword();
              }
          }
        else
          {
            authenticated = true;
          }
      }
    AuthenticationFailedException afex = null;
    try
      {
        success = protocolConnect(host, port, user, password);
      }
    catch (AuthenticationFailedException afex2)
      {
        afex = afex2;
      }
    if (!success)
      {
        InetAddress address = null;
        try
          {
            address = InetAddress.getByName(host);
          }
        catch (UnknownHostException e)
          {
          }
        PasswordAuthentication auth = 
          session.requestPasswordAuthentication(address, port, protocol,
                                                 null, user);
        if (auth != null)
          {
            user = auth.getUserName();
            password = auth.getPassword();
            success = protocolConnect(host, port, user, password);
          }
      }
    if (!success)
      {
        if (afex!=null)
          {
            throw afex;
          }
        throw new AuthenticationFailedException();
      }
    setURLName(new URLName(protocol, host, port, file, user, password));
    if (authenticated)
      {
        PasswordAuthentication auth =
          new PasswordAuthentication(user, password);
        session.setPasswordAuthentication(getURLName(), auth);
      }
    setConnected(true);
    notifyConnectionListeners(ConnectionEvent.OPENED);
  }
  
  /**
   * Provider implementation for a service.
   * <p>
   * This method should return <code>false</code> if authentication fails,
   * due to the username or password being unavailable or incorrect, or may
   * throw <code>AuthenticationFailedException</code> for further details.
   * <p>
   * In the case of failures not related to authentication, such as an
   * invalid configuration or network error, this method should throw an
   * appropriate <code>MessagingException</code>.
   * @param host the name of the host to connect to
   * @param port the port to use (-1 for the default port)
   * @param user the username
   * @param password the password
   * @return true on success, false if authentication failed
   * @exception AuthenticationFailedException on authentication failure
   * @exception MessagingException for non-authentication failures
   */
  protected boolean protocolConnect(String host, int port, 
                                     String user, String password)
    throws MessagingException
  {
    return false;
  }

  /**
   * Indicates whether this service is currently connected.
   */
  public boolean isConnected()
  {
    return connected;
  }

  /**
   * Sets the connection state of this service.
   */
  protected void setConnected(boolean connected)
  {
    this.connected = connected;
  }

  /**
   * Closes this service, terminating any underlying connections.
   */
  public synchronized void close()
    throws MessagingException
  {
    setConnected(false);
    notifyConnectionListeners(ConnectionEvent.CLOSED);
  }

  /**
   * Return a URLName representing this service. The password field will not
   * be returned.
   */
  public URLName getURLName()
  {
    if (url != null && (url.getPassword() != null || url.getFile() != null))
      {
        return new URLName(url.getProtocol(), url.getHost(), url.getPort(),
                           null, url.getUsername(), null);
      }
    return url;
  }

  /**
   * Set the URLName representing this service.
   * This method is called when the service has successfully connected.
   */
  protected void setURLName(URLName url)
  {
    this.url = url;
  }

  // -- Event management --

  /*
   * Because the propagation of events of different kinds in the JavaMail
   * API is so haphazard, I have here sacrificed a small time advantage for
   * readability and consistency.
   *
   * All the various propagation methods now call a method with a name based
   * on the eventual listener method name prefixed by 'fire', as is the
   * preferred pattern for usage of the EventListenerList in Swing.
   *
   * Note that all events are currently delivered synchronously, where in
   * Sun's implementation a different thread is used for event delivery.
   * 
   * TODO Examine the impact of this.
   */

  // -- Connection events --

  /**
   * Adds a listener for connection events on this service.
   */
  public void addConnectionListener(ConnectionListener l)
  {
    if (connectionListeners == null)
      {
        connectionListeners = new ArrayList();
      }
    synchronized (connectionListeners)
      {
        connectionListeners.add(l);
      }
  }

  /**
   * Removes a connection event listener.
   */
  public void removeConnectionListener(ConnectionListener l)
  {
    if (connectionListeners != null)
      {
        synchronized (connectionListeners)
          {
            connectionListeners.remove(l);
          }
      }
  }

  /**
   * Notify all connection listeners. 
   */
  protected void notifyConnectionListeners(int type)
  {
    ConnectionEvent event = new ConnectionEvent(this, type);
    switch (type)
      {
      case ConnectionEvent.OPENED:
        fireOpened(event);
        break;
      case ConnectionEvent.DISCONNECTED:
        fireDisconnected(event);
        break;
      case ConnectionEvent.CLOSED:
        fireClosed(event);
        break;
      }
  }

  /*
   * Propagates an OPENED ConnectionEvent to all registered listeners.
   */
  void fireOpened(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].opened(event);
          }
      }
  }

  /*
   * Propagates a DISCONNECTED ConnectionEvent to all registered listeners.
   */
  void fireDisconnected(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].disconnected(event);
          }
      }
  }

  /*
   * Propagates a CLOSED ConnectionEvent to all registered listeners.
   */
  void fireClosed(ConnectionEvent event)
  {
    if (connectionListeners != null)
      {
        ConnectionListener[] l = null;
        synchronized (connectionListeners)
          {
            l = new ConnectionListener[connectionListeners.size()];
            connectionListeners.toArray(l);
          }
        for (int i = 0; i < l.length; i++)
          {
            l[i].closed(event);
          }
      }
  }

  /**
   * Returns <code>getURLName.toString</code> if this service has a URLName,
   * otherwise returns the default <code>toString</code>.
   */
  public String toString()
  {
    URLName urlName = getURLName();
    return (urlName != null) ? urlName.toString() : super.toString();
  }

  /**
   * Adds the event and vector of listeners to be notified.
   */
  protected void queueEvent(MailEvent event, Vector vector)
  {
    for (Iterator i = vector.iterator(); i.hasNext(); )
      {
        event.dispatch(i.next());
      }
  }

}
