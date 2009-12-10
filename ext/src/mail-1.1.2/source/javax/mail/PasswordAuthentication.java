/*
 * PasswordAuthentication.java
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

/**
 * Container for a username/password combination.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public final class PasswordAuthentication
{

  /*
   * The username.
   */
  private String userName;

  /*
   * The password.
   */
  private String password;

  /**
   * Creates a new password authentication
   * @param userName the username
   * @param password the password
   */
  public PasswordAuthentication(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
  }

  /**
   * Returns the username.
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * Returns the password.
   */
  public String getPassword()
  {
    return password;
  }
  
}

