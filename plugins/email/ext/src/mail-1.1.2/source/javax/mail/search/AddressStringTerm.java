/*
 * AddressStringTerm.java
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

package javax.mail.search;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 * A string comparison of message addresses.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class AddressStringTerm
  extends StringTerm
{

  /**
   * Constructor.
   * @param pattern the address pattern for comparison
   */
  protected AddressStringTerm(String pattern)
  {
    super(pattern, true);
  }

  /**
   * Indicates whether the address pattern specified in the constructor is a
   * substring of the string representation of the given address.
   * @param a the address
   */
  protected boolean match(Address a)
  {
    if (a instanceof InternetAddress)
      {
        return super.match(((InternetAddress) a).toUnicodeString());
      }
    else
      {
        return super.match(a.toString());
      }
  }

  public boolean equals(Object other)
  {
    return ((other instanceof AddressStringTerm) && super.equals(other));
  }

}
