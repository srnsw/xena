/*
 * AddressTerm.java
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

/**
 * A comparison of message addresses.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class AddressTerm
  extends SearchTerm
{

  /**
   * The address.
   */
  protected Address address;

  protected AddressTerm(Address address)
  {
    this.address = address;
  }

  /**
   * Returns the address to match.
   */
  public Address getAddress()
  {
    return address;
  }

  /**
   * Returns true if the specified address matches the address specified in
   * this term.
   */
  protected boolean match(Address address)
  {
    return address.equals(this.address);
  }

  public boolean equals(Object other)
  {
    return ((other instanceof AddressTerm) && 
       ((AddressTerm) other).address.equals(address));
  }

  public int hashCode()
  {
    return address.hashCode();
  }
  
}

