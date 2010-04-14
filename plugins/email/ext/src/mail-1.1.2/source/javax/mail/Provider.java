/*
 * Provider.java
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

/**
 * A description of a messaging implementation that can store or send
 * messages.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class Provider
{

  /**
   * A provider type (STORE or TRANSPORT).
   */
  public static class Type
  {

    public static final Type STORE = new Type("Store");
    public static final Type TRANSPORT = new Type("Transport");

    private String type;

    private Type(String type)
    {
      this.type = type;
    }
    
  }

  private Type type;
  private String protocol;
  private String className;
  private String vendor;
  private String version;

  /**
   * Creates a new provider of the given type and protocol.
   * @param type the provider type
   * @param protocol the protocol URL scheme
   * @param className the name of the implementing class
   * @param vendor the implementation vendor
   * @param version the implementation version
   * @since JavaMail 1.4
   */
  public Provider(Type type, String protocol, String className, String vendor,
                  String version)
  {
    this.type = type;
    this.protocol = protocol;
    this.className = className;
    this.vendor = vendor;
    this.version = version;
  }

  /**
   * Returns the provider type.
   */
  public Type getType()
  {
    return type;
  }

  /**
   * Returns the protocol implemented by this provider.
   */
  public String getProtocol()
  {
    return protocol;
  }

  /**
   * Returns the name of the class implementing the protocol.
   */
  public String getClassName()
  {
    return className;
  }

  /**
   * Returns the name of the vendor associated with this implementation.
   */
  public String getVendor()
  {
    return vendor;
  }

  /**
   * Returns the version of this implementation.
   */
  public String getVersion()
  {
    return version;
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append("javax.mail.Provider[");
    if (type == Type.STORE)
      {
        buffer.append("STORE,");
      }
    else if (type == Type.TRANSPORT)
      {
        buffer.append("TRANSPORT,");
      }
    buffer.append(protocol);
    buffer.append(',');
    buffer.append(className);
    if (vendor != null)
      {
        buffer.append(',');
        buffer.append(vendor);
      }
    if (version != null)
      {
        buffer.append(',');
        buffer.append(version);
      }
    buffer.append("]");
    return buffer.toString();
  }
  
}

