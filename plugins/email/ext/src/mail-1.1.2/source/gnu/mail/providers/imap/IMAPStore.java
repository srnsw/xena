/*
 * IMAPStore.java
 * Copyright(C) 2003,2004 Chris Burdess <dog@gnu.org>
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URLDecoder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;
import javax.mail.event.StoreEvent;
import javax.net.ssl.TrustManager;

import gnu.inet.imap.IMAPConnection;
import gnu.inet.imap.IMAPConstants;
import gnu.inet.imap.MailboxStatus;
import gnu.inet.imap.Namespaces;
import gnu.inet.imap.Quota;

/**
 * The storage class implementing the IMAP4rev1 mail protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class IMAPStore
  extends Store
{

  /**
   * The connection to the IMAP server.
   */
  protected IMAPConnection connection = null;

  /**
   * Folder representing the root namespace of the IMAP connection.
   */
  protected IMAPFolder root = null;

  /**
   * The currently selected folder.
   */
  protected IMAPFolder selected = null;

  /**
   * Constructor.
   */
  public IMAPStore(Session session, URLName url)
  {
    super(session, url);
  }

  /**
   * Connects to the IMAP server and authenticates with the specified
   * parameters.
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
    if (host == null || username == null || password == null)
      {
        return false;
      }
    synchronized (this)
      {
        try
          {
            int connectionTimeout = getIntProperty("connectiontimeout");
            int timeout = getIntProperty("timeout");
            if (session.getDebug())
              {
                IMAPConnection.logger.setLevel(IMAPConnection.IMAP_TRACE);
              }
            boolean tls = "imaps".equals(url.getProtocol());
            // Locate custom trust manager
            TrustManager tm = getTrustManager();
            connection = new IMAPConnection(host, port,
                                            connectionTimeout, timeout,
                                            tls, tm);
            if (propertyIsTrue("debug.ansi"))
              {
                connection.setAnsiDebug(true);
              }
        
            List capabilities = connection.capability();

            // Ignore tls settings if we are making the connection
            // to a dedicated SSL port. (imaps)
            if (!tls && capabilities.contains(IMAPConstants.STARTTLS))
              {
                if (!propertyIsFalse("tls"))
                  {
                    if (tm == null)
                      {
                        tls = connection.starttls();
                      }
                    else
                      {
                        tls = connection.starttls(tm);
                      }
                    // Capabilities may have changed since STARTTLS
                    if (tls)
                      {
                        capabilities = connection.capability();
                      }
                  }
              }
            if (!tls && "required".equals(getProperty("tls")))
              {
                throw new MessagingException("TLS not available");
              }
            // Build list of available SASL mechanisms
            List authenticationMechanisms = null;
            for (Iterator i = capabilities.iterator(); i.hasNext(); )
              {
                String cap = (String) i.next();
                if (cap.startsWith("AUTH="))
                  {
                    if (authenticationMechanisms == null)
                      {
                        authenticationMechanisms = new ArrayList();
                      }
                    authenticationMechanisms.add(cap.substring(5));
                  }
              }
            // User authentication
            if (authenticationMechanisms != null &&
                !authenticationMechanisms.isEmpty())
              {
                if (username == null || password == null)
                  {
                    PasswordAuthentication pa =
                      session.getPasswordAuthentication(url);
                    if (pa == null)
                      {
                        InetAddress addr = InetAddress.getByName(host);
                        pa = session.requestPasswordAuthentication(addr,
                                                                    port,
                                                                    "imap",
                                                                    null,
                                                                    null);
                      }
                    if (pa != null)
                      {
                        username = pa.getUserName();
                        password = pa.getPassword();
                      }
                  }
                if (username != null && password != null)
                  {
                    // Discover user ordering preferences for auth
                    // mechanisms
                    String authPrefs = getProperty("auth.mechanisms");
                    Iterator i = null;
                    if (authPrefs == null)
                      {
                        i = authenticationMechanisms.iterator();
                      }
                    else
                      {
                        StringTokenizer st =
                          new StringTokenizer(authPrefs, ",");
                        List authPrefList = Collections.list(st);
                        i = authPrefList.iterator();
                      }
                    // Try each mechanism in the list in turn
                    while (i.hasNext())
                      {
                        String mechanism = (String) i.next();
                        if (authenticationMechanisms.contains(mechanism) &&
                            connection.authenticate(mechanism, username,
                                                     password))
                          {
                            return true;
                          }
                      }
                  }
              }
            if (capabilities.contains(IMAPConstants.LOGINDISABLED))
              {
                return false; // sorry
              }
            return connection.login(username, password);
          }
        catch (UnknownHostException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
        finally
          {
            if (connection != null && connection.alertsPending())
              {
                processAlerts();
              }
          }
      }
  }

  /**
   * Returns a trust manager used for TLS negotiation.
   */
  protected TrustManager getTrustManager()
    throws MessagingException
  {
    String tmt = getProperty("trustmanager");
    if (tmt == null)
      {
        return null;
      }
    else
      {
        try
          {
            // Instantiate the trust manager
            Class t = Class.forName(tmt);
            TrustManager tm = (TrustManager) t.newInstance();
            // If there is a setSession method, call it
            try
              {
                Class[] pt = new Class[] { Session.class };
                Method m = t.getMethod("setSession", pt);
                Object[] args = new Object[] { session };
                m.invoke(tm, args);
              }
            catch (NoSuchMethodException e)
              {
              }
            return tm;
          }
        catch (Exception e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Closes the connection.
   */
  public synchronized void close()
    throws MessagingException
  {
    if (connection != null)
      {
        synchronized (this)
          {
            try
              {
                connection.logout();
              }
            catch (IOException e)
              {
              }
            connection = null;
          }
      }
    super.close();
  }

  /**
   * Returns the root folder.
   */
  public Folder getDefaultFolder()
    throws MessagingException
  {
    if (root == null)
      {
        root = new IMAPFolder(this, "");
      }
    return root;
  }

  /**
   * Returns the folder with the specified name.
   */
  public Folder getFolder(String name)
    throws MessagingException
  {
    return new IMAPFolder(this, name);
  }

  /**
   * Returns the folder whose name is the file part of the specified URLName.
   */
  public Folder getFolder(URLName urlname)
    throws MessagingException
  {
    try
      {
        String file = URLDecoder.decode(urlname.getFile(), "UTF-8");
        return getFolder(file);
      }
    catch (UnsupportedEncodingException e)
      {
        throw new MessagingException(e.getMessage(), e);
      }
  }

  /**
   * Uses a NOOP to ensure that the connection to the IMAP server is still
   * valid.
   */
  public boolean isConnected()
  {
    if (!super.isConnected())
      return false;
    try
      {
        synchronized (this)
          {
            MailboxStatus ms = connection.noop();
            if (selected != null)
              {
                try
                  {
                    selected.update(ms, true);
                  }
                catch (MessagingException e)
                  {
                    // Ignore
                  }
              }
          }
        return true;
      }
    catch (IOException e)
      {
        return false;
      }
  }

  /**
   * Returns the IMAP connection used by this store.
   * @exception StoreClosedException if the store is not currently connected
   */
  protected IMAPConnection getConnection()
    throws StoreClosedException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    return connection;
  }

  /**
   * Indicates whether the specified folder is selected.
   */
  protected boolean isSelected(IMAPFolder folder)
  {
    return folder.equals(selected);
  }

  /**
   * Sets the selected folder.
   */
  protected void setSelected(IMAPFolder folder)
  {
    selected = folder;
  }

  /**
   * Process any alerts supplied by the server.
   */
  protected void processAlerts()
  {
    String[] alerts = connection.getAlerts();
    for (int i = 0; i < alerts.length; i++)
      {
        notifyStoreListeners(StoreEvent.ALERT, alerts[i]);
      }
  }

  /**
   * Returns a list of folders representing personal namespaces.
   * See RFC 2342 for details.
   */
  public Folder[] getPersonalNamespaces()
    throws MessagingException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    synchronized (this)
      {
        try
          {
            Namespaces ns = connection.namespace();
            if (ns == null)
              {
                throw new MethodNotSupportedException("IMAP NAMESPACE " +
                                                       "command not supported");
              }
            Namespaces.Namespace[] n = ns.getPersonal();
            if (n == null)
              return new Folder[0];
            Folder[] f = new Folder[n.length];
            for (int i = 0; i < n.length; i++)
              {
                String prefix = n[i].getPrefix();
                char delimiter = n[i].getDelimiter();
                f[i] = new IMAPFolder(this, prefix, delimiter);
              }
            return f;
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Returns a list of folders representing other users' namespaces.
   * See RFC 2342 for details.
   */
  public Folder[] getUserNamespaces()
    throws MessagingException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    synchronized (this)
      {
        try
          {
            Namespaces ns = connection.namespace();
            if (ns == null)
              {
                throw new MethodNotSupportedException("IMAP NAMESPACE " +
                                                       "command not supported");
              }
            Namespaces.Namespace[] n = ns.getOther();
            if (n == null)
              return new Folder[0];
            Folder[] f = new Folder[n.length];
            for (int i = 0; i < n.length; i++)
              {
                String prefix = n[i].getPrefix();
                char delimiter = n[i].getDelimiter();
                f[i] = new IMAPFolder(this, prefix, delimiter);
              }
            return f;
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Returns a list of folders representing shared namespaces.
   * See RFC 2342 for details.
   */
  public Folder[] getSharedNamespaces()
    throws MessagingException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    synchronized (this)
      {
        try
          {
            Namespaces ns = connection.namespace();
            if (ns == null)
              {
                throw new MethodNotSupportedException("IMAP NAMESPACE " +
                                                       "command not supported");
              }
            Namespaces.Namespace[] n = ns.getShared();
            if (n == null)
              return new Folder[0];
            Folder[] f = new Folder[n.length];
            for (int i = 0; i < n.length; i++)
              {
                String prefix = n[i].getPrefix();
                char delimiter = n[i].getDelimiter();
                f[i] = new IMAPFolder(this, prefix, delimiter);
              }
            return f;
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Returns the quota for the specified quota root.
   * @param root the quota root
   */
  public Quota getQuota(String root)
    throws MessagingException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    synchronized (this)
      {
        try
          {
            return connection.getquota(root);
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
  }

  /**
   * Sets the quota resource set for the specified quota root.
   * @param root the quota root
   * @param resources the quota resources to set
   */
  public void setQuota(String root, Quota.Resource[] resources)
    throws MessagingException
  {
    if (!super.isConnected())
      {
        throw new StoreClosedException(this);
      }
    synchronized (this)
      {
        try
          {
            connection.setquota(root, resources);
          }
        catch (IOException e)
          {
            throw new MessagingException(e.getMessage(), e);
          }
      }
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
        catch (Exception e)
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
    String value = session.getProperty("mail.imap." + key);
    if (value == null)
      {
        value = session.getProperty("mail." + key);
      }
    return value;
  }

}
