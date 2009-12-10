/*
 * URLName.java
 * Copyright(C) 2002 The Free Software Foundation
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

package javax.mail;

import java.io.*;
import java.net.*;

/**
 * The name of a URL.
 * This class represents a URL name and also provides the basic
 * parsing functionality to parse most internet standard URL schemes.
 * <p>
 * Note that this class differs from java.net.URL in that this class just
 * represents the name of a URL, it does not model the connection to a URL.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class URLName
{

  /**
   * The full version of the URL
   */
  protected String fullURL;

  private String protocol;
  private String host;
  private int port = -1;
  private String file;
  private String ref;
  private String username;
  private String password;

  private InetAddress hostAddress;
  private boolean gotHostAddress = false;
  
  
  private int hashCode;

  /**
   * Creates a URLName object from the specified protocol, host,
   * port number, file, username, and password.
   * Specifying a port number of -1 indicates that the URL should 
   * use the default port for the protocol.
   */
  public URLName(String protocol, String host, int port,
                  String file, String username, String password)
  {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.file = file;
    if (file != null)
      {
        int hashIndex = file.indexOf('#');
        if (hashIndex != -1)
          {
            this.file = file.substring(0, hashIndex);
            ref = file.substring(hashIndex + 1);
          }
      }
    this.username = username;
    this.password = password;
  }

  /**
   * Construct a URLName from a java.net.URL object.
   */
  public URLName(URL url)
  {
    this(url.toString());
  }

  /**
   * Construct a URLName from the string. Parses out all the possible
   * information(protocol, host, port, file, username, password).
   */
  public URLName(String url)
  {
    parseString(url);
  }

  /**
   * Returns the port number of this URLName.
   * Returns -1 if the port is not set.
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Returns the protocol of this URLName. 
   * Returns null if this URLName has no protocol.
   */
  public String getProtocol()
  {
    return protocol;
  }

  /**
   * Returns the file name of this URLName.
   * Returns null if this URLName has no file name.
   */
  public String getFile()
  {
    return file;
  }

  /**
   * Returns the reference of this URLName.
   * Returns null if this URLName has no reference.
   */
  public String getRef()
  {
    return ref;
  }

  /**
   * Returns the host of this URLName.
   * Returns null if this URLName has no host.
   */
  public String getHost()
  {
    return host;
  }

  /**
   * Returns the user name of this URLName.
   * Returns null if this URLName has no user name.
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Returns the password of this URLName.
   * Returns null if this URLName has no password.
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Constructs a URL from the URLName.
   */
  public URL getURL()
    throws MalformedURLException
  {
    return new URL(getProtocol(), getHost(), getPort(), getFile());
  }

  // -- Utility methods --

  /**
   * Constructs a string representation of this URLName.
   */
  public String toString()
  {
    if (fullURL == null)
      {
        StringBuffer buffer = new StringBuffer();
        if (protocol != null)
          {
            buffer.append(protocol);
            buffer.append(":");
          }
        if (username != null || host != null)
          {
            buffer.append("//");
            if (username != null)
              {
                buffer.append(username);
                if (password != null)
                  {
                    buffer.append(":");
                    buffer.append(password);
                  }
                buffer.append("@");
              }
            if (host != null)
              {
                buffer.append(host);
              }
            if (port != -1)
              {
                buffer.append(":");
                buffer.append(Integer.toString(port));
              }
            if (file != null)
              {
                buffer.append("/");
              }
          }
        if (file != null)
          {
            buffer.append(file);
          }
        if (ref != null)
          {
            buffer.append("#");
            buffer.append(ref);
          }
        fullURL = buffer.toString();
      }
    return fullURL;
  }
  
  /**
   * Compares two URLNames. The result is true if and only if the argument is
   * not null and is a URLName object that represents the same URLName as this
   * object. Two URLName objects are equal if they have the same protocol and
   * reference the same host, the same port number on the host, the same
   * username and password, and the same file on the host. The fields(host,
   * username, password, file) are also considered the same if they are both
   * null.
   */
  public boolean equals(Object other)
  {
    if (other == this)
      {
        return true;
      }
    if (!(other instanceof URLName))
      {
        return false;
      }
    URLName url = (URLName) other;
    if (url.protocol == null || !url.protocol.equals(protocol))
      {
        return false;
      }
    InetAddress address = getHostAddress();
    InetAddress otherAddress = url.getHostAddress();
    if (address != null && otherAddress != null)
      {
        if (!address.equals(otherAddress))
          {
            return false;
          }
      }
    else if (host != null)
      {
        if (!host.equalsIgnoreCase(url.host))
          {
            return false;
          }
      }
    if (username != url.username && 
       (username==null || !username.equals(url.username)))
      {
        return false;
      }
    String fileNormalized = (file != null) ? file : "";
    String otherFile = (url.file != null) ? url.file : "";
    if (!fileNormalized.equals(otherFile))
      {
        return false;
      }
    return port == url.port;
  }

  /**
   * Compute the hash code for this URLName.
   */
  public int hashCode()
  {
    if (hashCode != 0)
      {
        return hashCode;
      }
    if (protocol != null)
      {
        hashCode += protocol.hashCode();
      }
    InetAddress address = getHostAddress();
    if (address != null)
      {
        hashCode += address.hashCode();
      }
    else if (host != null)
      {
        hashCode += host.toLowerCase().hashCode();
      }
    if (username != null)
      {
        hashCode += username.hashCode();
      }
    if (file != null)
      {
        hashCode += file.hashCode();
      }
    hashCode += port;
    return hashCode;
  }

  private synchronized InetAddress getHostAddress()
  {
    if (gotHostAddress)
      {
        return hostAddress;
      }
    if (host == null)
      {
        return null;
      }
    try
      {
        hostAddress = InetAddress.getByName(host);
      }
    catch (UnknownHostException e)
      {
        hostAddress = null;
      }
    gotHostAddress = true;
    return hostAddress;
  }

  /**
   * Method which does all of the work of parsing the string.
   */
  protected void parseString(String url)
  {
    protocol = file = ref = host = username = password = null;
    port = -1;
    int len = url.length();
    int colonIndex = url.indexOf(':');
    if (colonIndex != -1)
      {
        protocol = url.substring(0, colonIndex);
      }
    if (url.regionMatches(colonIndex + 1, "//", 0, 2))
      {
        String hostPart;
        int slashIndex = url.indexOf('/', colonIndex + 3);
        if (slashIndex != -1)
          {
            hostPart = url.substring(colonIndex + 3, slashIndex);
            if ((slashIndex + 1) < len)
              {
                file = url.substring(slashIndex + 1);
              }
            else
              {
                file = "";
              }
          }
        else
          {
            hostPart = url.substring(colonIndex + 3);
          }
        
        // user:password@host?
        int atIndex = hostPart.lastIndexOf('@');
        if (atIndex != -1)
          {
            String userPart = hostPart.substring(0, atIndex);
            hostPart = hostPart.substring(atIndex + 1);
            colonIndex = userPart.indexOf(':');
            if (colonIndex != -1)
              {
                username = userPart.substring(0, colonIndex);
                password = userPart.substring(colonIndex + 1);
              }
            else
              {
                username = userPart;
              }
          }
        
        // host:port?
        if (hostPart.length() > 0 && hostPart.charAt(0) == '[')
          {
            colonIndex = hostPart.indexOf(':', hostPart.indexOf(']'));
          }
        else
          {
            colonIndex = hostPart.indexOf(':');
          }
        if (colonIndex != -1)
          {
            String portPart = hostPart.substring(colonIndex + 1);
            if (portPart.length() > 0)
              {
                try
                  {
                    port = Integer.parseInt(portPart);
                  }
                catch (NumberFormatException e)
                  {
                    port = -1;
                  }
              }
            host = hostPart.substring(0, colonIndex);
          }
        else
          {
            host = hostPart;
          }
      }
    else if ((colonIndex + 1) < len)
      {
        file = url.substring(colonIndex + 1);
      }
    
    int hashIndex = (file != null) ? file.indexOf('#') : -1;
    if (hashIndex != -1)
      {
        ref = file.substring(hashIndex + 1);
        file = file.substring(0, hashIndex);
      }
  }
  
}
