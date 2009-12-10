/*
 * ACL.java
 * Copyright (C) 2005 Chris Burdess <dog@gnu.org>
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

package gnu.mail.providers.imap;

/**
 * An access control list <b>entry</b>.
 * Please note that this API is <i>experimental</i> and will probably change
 * soon when the IETF working group delivers a a new specification.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @deprecated this API will probably change incompatibly soon
 */
public final class ACL
{

  String name;
  Rights rights;

  public ACL(String name)
  {
    this(name, null);
  }

  public ACL(String name, Rights rights)
  {
    this.name = name;
    this.rights = rights;
  }

  public String getName()
  {
    return name;
  }

  public void setRights(Rights rights)
  {
    this.rights = rights;
  }

  public Rights getRights()
  {
    return rights;
  }
  
}

