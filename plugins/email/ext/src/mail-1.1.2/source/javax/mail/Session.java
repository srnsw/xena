/*
 * Session.java
 * Copyright (C) 2002, 2004, 2005 The Free Software Foundation
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

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A session represents the context of a messaging transaction. It provides
 * a repository of configuration information in the form of properties.
 *
 * @author Andrew Selkirk
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @author <a href="mailto:nferrier@tapsellferrier.co.uk">Nic Ferrier</a>
 * @version 1.4
 */
public final class Session
{

  // Constant definitions of property locations.
  
  private static final String SYSTEM_PROVIDERS =
     (System.getProperty("java.home")
			+ File.separator
			+ "lib"
			+ File.separator
			+ "javamail.providers");
      
  private static final String CUSTOM_PROVIDERS =
      "META-INF/javamail.providers";
  
  private static final String DEFAULT_PROVIDERS =
      "META-INF/javamail.default.providers";
  
  private static final String SYSTEM_ADDRESS_MAP =
     (System.getProperty("java.home")
			+ File.separator
			+ "lib"
			+ File.separator
			+ "javamail.address.map");
  
  private static final String CUSTOM_ADDRESS_MAP =
      "META-INF/javamail.address.map";
  
  private static final String DEFAULT_ADDRESS_MAP =
      "META-INF/javamail.default.address.map";

  // Class data.

  private Properties props;
  
  private Authenticator authenticator;

  private HashMap authTable = new HashMap();
  
  private boolean debug;
  
  private ArrayList providers = new ArrayList();

  private HashMap providersByProtocol = new HashMap();

  private HashMap providersByClassName = new HashMap();

  private Properties addressMap = new Properties();

  private static Session defaultSession = null;

  private Logger logger = Logger.getLogger(Session.class.getName());

  /**
   * Create the session.
   * @param props the session properties
   * @param authenticator callback for authentication
   */
  private Session(Properties props, Authenticator authenticator)
  {
    this.props = props;
    this.authenticator = authenticator;
    debug = new Boolean(props.getProperty("mail.debug")).booleanValue();
    logger.setLevel(debug ? Level.FINER : Level.SEVERE);
    logger.info("using GNU JavaMail 1.3");
    ClassLoader loader = null;
    if (authenticator == null)
      {
        loader = getClass().getClassLoader();
      }
    else
      {
        loader = authenticator.getClass().getClassLoader();
      }
    // Load the providers
    loadProviders(getResourceAsStream(loader, DEFAULT_PROVIDERS), "default");
    loadProviders(getResourceAsStream(loader, CUSTOM_PROVIDERS), "custom");
    try
      {
        File file = new File(SYSTEM_PROVIDERS);
        InputStream pin = new BufferedInputStream(new FileInputStream(file));
        loadProviders(pin, "system");
      }
    catch (FileNotFoundException e)
      {
        logger.log(Level.WARNING, "no system providers", e);
      }
    logger.log(Level.FINE, "Providers by class name: "
               + providersByClassName.toString());
    logger.log(Level.FINE, "Providers by protocol: "
               + providersByProtocol.toString());
    // Load the address map
    loadAddressMap(getResourceAsStream(loader, DEFAULT_ADDRESS_MAP),
                   "default");
    loadAddressMap(getResourceAsStream(loader, CUSTOM_ADDRESS_MAP),
                   "custom");
    try
      {
        File file = new File(SYSTEM_ADDRESS_MAP);
        InputStream min = new BufferedInputStream(new FileInputStream(file));
        loadAddressMap(min, "system");
      }
    catch (FileNotFoundException e)
      {
        logger.log(Level.WARNING, "no system address map", e);
      }
  }

  /**
   * Returns an input stream for a resource name.
   * <code>ClassLoader.getResourceAsStream</code> should work,
   * but Class.getClassLoader() returns null in kaffe (2003-01-22).
   */
  private InputStream getResourceAsStream(ClassLoader loader, String resource)
  {
    InputStream in = null;
    try
      {
        if (loader == null)
          {
            in = loader.getResourceAsStream(resource);
          }
        else
          {
            in = getClass().getResourceAsStream(resource);
          }
        if (in == null && resource.charAt(0) != '/')
          {
            in = getResourceAsStream(loader, "/" + resource);
          }
      }
    catch (Exception e)
      {
      }
    return in;
  }
  
  /**
   * Loads the provider database description.
   */
  private void loadProviders(InputStream in, String description)
  {
    if (in == null)
      {
        logger.info("no " + description + " providers");
        return;
      }
    try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        for (String line = reader.readLine();
            line != null;
            line = reader.readLine()) 
          {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 0)
              {
                Provider.Type type = null;
                String protocol = null;
                String className = null;
                String vendor = null;
                String version = null;
                for (StringTokenizer st = new StringTokenizer(line, ";"); 
                     st.hasMoreTokens(); )
                  {
                    String token = st.nextToken().trim();
                    int equalsIndex = token.indexOf("=");
                    if (token.startsWith("protocol="))
                      {
                        protocol = token.substring(equalsIndex + 1);
                      }
                    else if (token.startsWith("type="))
                      {
                        String transportValue =
                          token.substring(equalsIndex + 1);
                        if (transportValue.equalsIgnoreCase("store"))
                          {
                            type = Provider.Type.STORE;
                          }
                        else if (transportValue.equalsIgnoreCase("transport"))
                          {
                            type = Provider.Type.TRANSPORT;
                          }
                      }
                    else if (token.startsWith("class="))
                      {
                        className = token.substring(equalsIndex + 1);
                      }
                    else if (token.startsWith("vendor="))
                      {
                        vendor = token.substring(equalsIndex + 1);
                      }
                    else if (token.startsWith("version="))
                      {
                        version = token.substring(equalsIndex + 1);
                      }
                  }
                
                if (type == null || protocol == null || className == null)
                  {
                    logger.warning("Invalid provider: " + line);
                  }
                else
                  {
                    Provider provider = new Provider(type, protocol, className,
                                                      vendor, version);
                    providers.add(provider);
                    providersByClassName.put(className, provider);
                    if (!providersByProtocol.containsKey(protocol))
                      {
                        providersByProtocol.put(protocol, provider);
                      }
                  }
              }
          }
        in.close();
        logger.info("loaded " + description + " providers");
      }
    catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    catch (SecurityException e)
      {
        logger.log(Level.WARNING, "can't load " + description +
                   " providers", e);
      }
  }
  
  private void loadAddressMap(InputStream in, String description)
  {
    if (in == null)
      {
        logger.info("no " + description + " address map");
        return;
      }
    try
      {
        addressMap.load(in);
        in.close();
        logger.info("loaded " + description + " address map");
      }
    catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    catch (SecurityException e)
      {
        logger.log(Level.WARNING, "can't load " + description +
                   " address map", e);
      }
  }
  
  /**
   * Returns a new session.
   * @param props a properties object holding relevant properties.
   * It is expected that the client supplies values for the properties
   * listed in Appendix A of the JavaMail spec (particularly
   * <code>mail.store.protocol</code>, 
   * <code>mail.transport.protocol</code>,
   * <code>mail.host</code>,
   * <code>mail.user</code>,
   * and <code>mail.from</code>)
   * as the defaults are unlikely to work in all cases.
   * @param authenticator an authenticator used to call back to the
   * application when a user name and password is needed.
   */
  public static Session getInstance(Properties props, 
      Authenticator authenticator)
  {
    return new Session(props, authenticator);
  }

  /**
   * Returns a new Session.
   * @param props a properties object holding relevant properties.
   * It is expected that the client supplies values for the properties
   * listed in Appendix A of the JavaMail spec (particularly
   * <code>mail.store.protocol</code>, 
   * <code>mail.transport.protocol</code>,
   * <code>mail.host</code>,
   * <code>mail.user</code>,
   * and <code>mail.from</code>)
   * as the defaults are unlikely to work in all cases.
   */
  public static Session getInstance(Properties props)
  {
    return getInstance(props, null);
  }

  /**
   * Returns the default session.
   * If a default has not yet been setup, a new session is created 
   * and installed as the default.
   * <p>
   * Since the default session is potentially available to all code 
   * executing in the same Java virtual machine, and the session can 
   * contain security sensitive information such as user names and 
   * passwords, access to the default session is restricted. 
   * The Authenticator object, which must be created by the caller, 
   * is used indirectly to check access permission. The Authenticator 
   * object passed in when the session is created is compared with
   * the Authenticator object passed in to subsequent requests to get the
   * default session. If both objects are the same, or are from the same
   * ClassLoader, the request is allowed. Otherwise, it is denied.
   * <p>
   * Note that if the Authenticator object used to create the session is null,
   * anyone can get the default session by passing in null.
   * <p>
   * In JDK 1.2, additional security Permission objects may be used to control
   * access to the default session.
   * @param props Properties object that hold relevant properties.
   * It is expected that the client supplies values for the properties
   * listed in Appendix A of the JavaMail spec (particularly
   * <code>mail.store.protocol</code>, 
   * <code>mail.transport.protocol</code>,
   * <code>mail.host</code>,
   * <code>mail.user</code>,
   * and <code>mail.from</code>)
   * as the defaults are unlikely to work in all cases.
   * @param authenticator Authenticator object used to call back to the
   * application when a user name and password is needed.
   */
  public static Session getDefaultInstance(Properties props,
                                           Authenticator authenticator)
  {
    if (defaultSession == null)
      {
        defaultSession = new Session(props, authenticator);
      }
    else if (defaultSession.authenticator != authenticator
             &&(defaultSession.authenticator == null ||
                authenticator == null ||
                (defaultSession.authenticator.getClass().getClassLoader()
                 != authenticator.getClass().getClassLoader())))
      {
        throw new SecurityException("Access denied");
      }
    return defaultSession;
  }
  
  /**
   * Get the default Session object.
   * If a default has not yet been setup, a new Session object is created 
   * and installed as the default.
   * <p>
   * Note that a default session created with no Authenticator is available 
   * to all code executing in the same Java virtual machine, and the session 
   * can contain security sensitive information such as user names and 
   * passwords.
   * @param props Properties object that hold relevant properties.
   * It is expected that the client supplies values for the properties
   * listed in Appendix A of the JavaMail spec(particularly
   * <code>mail.store.protocol</code>, 
   * <code>mail.transport.protocol</code>,
   * <code>mail.host</code>,
   * <code>mail.user</code>,
   * and <code>mail.from</code>)
   * as the defaults are unlikely to work in all cases.
   */
  public static Session getDefaultInstance(Properties props)
  {
    return getDefaultInstance(props, null);
  }

  /**
   * Set the debug setting for this Session.
   * <p>
   * Since the debug setting can be turned on only after the Session has been
   * created, to turn on debugging in the Session constructor, set the property
   * <code>mail.debug</code> in the Properties object passed in to the 
   * constructor to true. The value of the <code>mail.debug</code> property 
   * is used to initialize the per-Session debugging flag. Subsequent calls 
   * to the <code>setDebug</code> method manipulate the per-Session debugging 
   * flag and have no affect on the <code>mail.debug</code> property.
   */
  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  /**
   * Get the debug setting for this Session.
   */
  public boolean getDebug()
  {
    return debug;
  }

  /**
   * This method returns an array of all the implementations installed 
   * via the javamail.[default.]providers files that can be loaded 
   * using the ClassLoader available to this application.
   */
  public Provider[] getProviders()
  {
    Provider[] p = new Provider[providers.size()];
    providers.toArray(p);
    return p;
  }

  /**
   * Returns the default Provider for the protocol specified.
   * Checks <code>mail.&lt;protocol&gt;.class</code> property first 
   * and if it exists, returns the Provider associated with 
   * this implementation. If it doesn't exist, returns the Provider that 
   * appeared first in the configuration files. 
   * If an implementation for the protocol isn't found, 
   * throws NoSuchProviderException
   * @param protocol Configured protocol(i.e. smtp, imap, etc)
   * @param NoSuchProviderException If a provider for the given protocol 
   * is not found.
   */
  public Provider getProvider(String protocol)
    throws NoSuchProviderException
  {
    if (protocol == null || protocol.length() <= 0)
      {
        throw new NoSuchProviderException("Invalid protocol: " + protocol);
      }
    Provider provider = null;
    String providerClassKey = "mail." + protocol + ".class";
    String providerClassName = props.getProperty(providerClassKey);
    synchronized (providers)
      {
        if (providerClassName != null)
          {
            provider = (Provider) providersByClassName.get(providerClassName);
          }
        if (provider == null)
          {
            provider = (Provider) providersByProtocol.get(protocol);
          }
      }
    if (provider == null)
      {
        throw new NoSuchProviderException("No provider for " + protocol);
      }
    return provider;
  }

  /**
   * Set the passed Provider to be the default implementation for the protocol
   * in Provider.protocol overriding any previous values.
   */
  public void setProvider(Provider provider)
    throws NoSuchProviderException
  {
    if (provider == null)
      {
        throw new NoSuchProviderException("Can't set null provider");
      }
    synchronized (providers)
      {
        String protocol = provider.getProtocol();
        providersByProtocol.put(protocol, provider);
        String providerClassKey = "mail." + protocol + ".class";
        props.put(providerClassKey, provider.getClassName());
      }
  }

  /**
   * Get a Store object that implements this user's desired Store protocol.
   * The <code>mail.store.protocol</code> property specifies the desired 
   * protocol. If an appropriate Store object is not obtained,
   * NoSuchProviderException is thrown
   */
  public Store getStore()
    throws NoSuchProviderException
  {
    return getStore(getProperty("mail.store.protocol"));
  }

  /**
   * Get a Store object that implements the specified protocol.
   * If an appropriate Store object cannot be obtained,
   * NoSuchProviderException is thrown.
   */
  public Store getStore(String protocol)
    throws NoSuchProviderException
  {
    return getStore(new URLName(protocol, null, -1, null, null, null));
  }

  /**
   * Get a Store object for the given URLName.
   * If the requested Store object cannot be obtained,
   * NoSuchProviderException is thrown. 
   * The "scheme" part of the URL string(Refer RFC 1738) is used to 
   * locate the Store protocol.
   * @param url URLName that represents the desired Store
   */
  public Store getStore(URLName url)
    throws NoSuchProviderException
  {
    String protocol = url.getProtocol();
    Provider provider = getProvider(protocol);
    return getStore(provider, url);
  }

  /**
   * Get an instance of the store specified by Provider.
   * Instantiates the store and returns it.
   * @param provider Store Provider that will be instantiated
   */
  public Store getStore(Provider provider)
    throws NoSuchProviderException
  {
    return getStore(provider, null);
  }

  private Store getStore(Provider provider, URLName url)
    throws NoSuchProviderException
  {
    if (provider == null || provider.getType() != Provider.Type.STORE)
      {
        throw new NoSuchProviderException("invalid provider");
      }
    try
      {
        return (Store) getService(provider, url);
      }
    catch (ClassCastException e)
      {
        throw new NoSuchProviderException("not a store");
      }
  }
  
  /**
   * Get a Transport object that implements this user's desired Transport
   * protocol.
   * The <code>mail.transport.protocol</code> property specifies the desired
   * protocol. If an appropriate Transport object cannot be obtained,
   * MessagingException is thrown.
   * @exception NoSuchProviderException If the provider is not found.
   */
  public Transport getTransport()
    throws NoSuchProviderException
  {
    return getTransport(getProperty("mail.transport.protocol"));
  }

  /**
   * Get a Transport object that implements the specified protocol.
   * If an appropriate Transport object cannot be obtained, null is returned.
   * @exception NoSuchProviderException If the provider is not found.
   */
  public Transport getTransport(String protocol)
    throws NoSuchProviderException
  {
    return getTransport(new URLName(protocol, null, -1, null, null, null));
  }

  /**
   * Get a Transport object for the given URLName.
   * If the requested Transport object cannot be obtained,
   * NoSuchProviderException is thrown. The "scheme" part of the URL 
   * string(Refer RFC 1738) is used to locate the Transport protocol.
   * @param url URLName that represents the desired Transport
   * @exception NoSuchProviderException If the provider is not found.
   */
  public Transport getTransport(URLName url)
    throws NoSuchProviderException
  {
    String protocol = url.getProtocol();
    Provider provider = getProvider(protocol);
    return getTransport(provider, url);
  }

  /**
   * Get an instance of the transport specified in the Provider.
   * Instantiates the transport and returns it.
   * @exception NoSuchProviderException If the provider is not found.
   */
  public Transport getTransport(Provider provider)
    throws NoSuchProviderException
  {
    return getTransport(provider, null);
  }

  /**
   * Get a Transport object that can transport a Message to the specified
   * address type.
   * @exception NoSuchProviderException If the provider is not found.
   */
  public Transport getTransport(Address address)
    throws NoSuchProviderException
  {
    String provider = (String) addressMap.get(address.getType());
    if (provider == null)
      {
        throw new NoSuchProviderException("No provider for address: "+
                                           address.getType());
      }
    return getTransport(provider);
  }

  private Transport getTransport(Provider provider, URLName urlname)
    throws NoSuchProviderException
  {
    if (provider == null || provider.getType() != Provider.Type.TRANSPORT)
      {
        throw new NoSuchProviderException("invalid provider");
      }
    try
      {
        return (Transport) getService(provider, urlname);
      }
    catch (ClassCastException _ex)
      {
        throw new NoSuchProviderException("incorrect class");
      }
  }

  /**
   * Get a closed Folder object for the given URLName.
   * If the requested Folder object cannot be obtained, null is returned.
   * <p>
   * The "scheme" part of the URL string(Refer RFC 1738) is used to locate 
   * the Store protocol. The rest of the URL string(that is, the
   * "schemepart", as per RFC 1738) is used by that Store in a protocol 
   * dependent manner to locate and instantiate the appropriate Folder object.
   * <p>
   * Note that RFC 1738 also specifies the syntax for the "schemepart" for
   * IP-based protocols(IMAP4, POP3, etc.). Providers of IP-based mail Stores
   * should implement that syntax for referring to Folders.
   * @param url URLName that represents the desired folder
   * @exception NoSuchProviderException If a provider for the given URLName 
   * is not found.
   * @param MessagingException if the Folder could not be located or created.
   */
  public Folder getFolder(URLName url)
    throws MessagingException
  {
    Store store = getStore(url);
    store.connect();
    return store.getFolder(url);
  }

  private Object getService(Provider provider, URLName url)
    throws NoSuchProviderException
  {
    if (provider == null)
      {
        throw new NoSuchProviderException("null");
      }
    if (url == null)
      {
        url = new URLName(provider.getProtocol(), null, -1, null, null,
                           null);
      }
    
    Class providerClass = null;
    ClassLoader loader;
    if (authenticator != null)
      {
        loader = authenticator.getClass().getClassLoader();
      }
    else
      {
        loader = getClass().getClassLoader();
      }
    try
      {
        providerClass = loader.loadClass(provider.getClassName());
      }
    catch (Exception e)
      {
        try
          {
            providerClass = Class.forName(provider.getClassName());
          }
        catch (Exception e2)
          {
            if (debug)
              {
                e2.printStackTrace();
              }
            throw new NoSuchProviderException(provider.getProtocol());
          }
      }
    try
      {
        Class[] parameterTypes = {
          javax.mail.Session.class, javax.mail.URLName.class
        };
        Constructor constructor = providerClass.getConstructor(parameterTypes);
        Object[] parameters = {
          this, url
        };
        return constructor.newInstance(parameters);
      }
    catch (Exception e)
      {
        if (debug)
          {
            e.printStackTrace();
          }
        throw new NoSuchProviderException(provider.getProtocol());
      }
  }

  /**
   * Save a PasswordAuthentication for this(store or transport) URLName.
   * If <code>pw</code> is null the entry corresponding to the URLName 
   * is removed.
   * <p>
   * This is normally used only by the store or transport implementations to
   * allow authentication information to be shared among multiple uses of a
   * session.
   */
  public void setPasswordAuthentication(URLName url,
                                         PasswordAuthentication pw)
  {
    if (pw == null)
      {
        authTable.remove(url);
      }
    else
      {
        authTable.put(url, pw);
      }
  }

  /**
   * Return any saved PasswordAuthentication for this(store or transport)
   * URLName. Normally used only by store or transport implementations.
   */
  public PasswordAuthentication getPasswordAuthentication(URLName url)
  {
    return (PasswordAuthentication) authTable.get(url);
  }

  /**
   * Call back to the application to get the needed user name and password.
   * The application should put up a dialog something like:
   * <pre>
   Connecting to <protocol> mail service on host <addr>, port <port>.
   <prompt>
   
   User Name: <defaultUserName>
   Password:
   * @param addr InetAddress of the host. may be null.
   * @param protocol protocol scheme(e.g. imap, pop3, etc.)
   * @param prompt any additional String to show as part of the prompt; may be
   * null.
   * @param defaultUserName the default username. may be null.
   */
  public PasswordAuthentication requestPasswordAuthentication(
      InetAddress address, int port, String protocol, String prompt,
      String defaultUserName)
  {
    if (authenticator != null)
      {
        return authenticator.requestPasswordAuthentication(address, port, 
                                                           protocol, prompt,
                                                           defaultUserName);
      }
    return null;
  }
  
  /**
   * Returns the Properties object associated with this Session.
   */
  public Properties getProperties()
  {
    return props;
  }
  
  /**
   * Returns the value of the specified property.
   * Returns null if this property does not exist.
   */
  public String getProperty(String name)
  {
    return props.getProperty(name);
  }

  /**
   * Set the stream to be used for debugging output for this session.
   * If <code>out</code> is null, <code>System.out</code> will be used. Note
   * that debugging output that occurs before any session is created, as a
   * result of setting the <code>mail.debug</code> property, will always be
   * sent to <code>System.out</code>.
   * @param out the PrintStream to use for debugging output
   * @since JavaMail 1.3
   */
  public void setDebugOut(PrintStream out)
  {
    if (out == null)
      {
        out = System.out;
      }
    // TODO
  }

  /**
   * Returns the stream to be used for debugging output. If no stream has
   * been set, <code>System.out</code> is returned.
   * @since JavaMail 1.3
   */
  public PrintStream getDebugOut()
  {
    // TODO
    return System.out;
  }

  /**
   * Adds the specified provider to the session.
   * @param provider the new provider
   * @since JavaMail 1.4
   */
  public void addProvider(Provider provider)
  {
    String protocol = provider.getProtocol();
    String className = provider.getClassName();
    providers.add(provider);
    providersByClassName.put(className, provider);
    if (!providersByProtocol.containsKey(protocol))
      {
        providersByProtocol.put(protocol, provider);
      }
  }

  /**
   * Sets the default transport protocol for the given address type.
   * @param addressType the type of address, e.g. "rfc822"
   * @param protocol the transport protocol to use
   * @since JavaMail 1.4
   */
  public void setProtocolForAddress(String addressType, String protocol)
  {
    addressMap.put(addressType, protocol);
  }

}
