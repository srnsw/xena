/*
 * Authenticator.java
 * Copyright (C) 2002 The Free Software Foundation
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

/**
 * Callback object that can be used to obtain password information during
 * authentication. This normally occurs by prompting the user for a password
 * or retrieving it from secure storage.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class Authenticator
{

  private String defaultUserName;
  private int requestingPort = -1;
  private String requestingPrompt;
  private String requestingProtocol;
  private InetAddress requestingSite;

  final PasswordAuthentication requestPasswordAuthentication(
      InetAddress requestingSite, 
      int requestingPort, 
      String requestingProtocol, 
      String requestingPrompt, 
      String defaultUserName)
  {
    this.requestingSite = requestingSite;
    this.requestingPort = requestingPort;
    this.requestingProtocol = requestingProtocol;
    this.requestingPrompt = requestingPrompt;
    this.defaultUserName = defaultUserName;
    return getPasswordAuthentication();
  }

  /**
   * Returns the default user name.
   */
  protected final String getDefaultUserName()
  {
    return defaultUserName;
  }

  /**
   * Called when password authentication is needed.
   * This method should be overridden by subclasses.
   */
  protected PasswordAuthentication getPasswordAuthentication()
  {
    return null;
  }

  /**
   * Returns the port used in the request.
   */
  protected final int getRequestingPort()
  {
    return requestingPort;
  }

  /**
   * Returns the user prompt string for the request.
   */
  protected final String getRequestingPrompt()
  {
    return requestingPrompt;
  }

  /**
   * Returns the protocol for the request.
   */
  protected final String getRequestingProtocol()
  {
    return requestingProtocol;
  }

  /**
   * Returns the IP address of the request host,
   * or null if not available.
   */
  protected final InetAddress getRequestingSite()
  {
    return requestingSite;
  }

}
