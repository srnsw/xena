/*
 * Rights.java
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

import gnu.inet.imap.IMAPConstants;

/**
 * A set of rights, as defined in RFC 2086.
 * Please note that this API is <i>experimental</i> and will probably change
 * soon when the IETF working group delivers a a new specification.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @deprecated this API will probably change incompatibly soon
 */
public final class Rights
{

  public static final class Right
  {

    public static final Right LOOKUP = new Right(IMAPConstants.RIGHTS_LOOKUP);
    public static final Right READ = new Right(IMAPConstants.RIGHTS_READ);
    public static final Right KEEP_SEEN = new Right(IMAPConstants.RIGHTS_SEEN);
    public static final Right WRITE = new Right(IMAPConstants.RIGHTS_WRITE);
    public static final Right INSERT = new Right(IMAPConstants.RIGHTS_INSERT);
    public static final Right POST = new Right(IMAPConstants.RIGHTS_POST);
    public static final Right CREATE = new Right(IMAPConstants.RIGHTS_CREATE);
    public static final Right DELETE = new Right(IMAPConstants.RIGHTS_DELETE);
    public static final Right ADMINISTER =
      new Right(IMAPConstants.RIGHTS_ADMIN);;

    final int code;

    Right(int code)
    {
      this.code = code;
    }

    public static Right getInstance(char right)
    {
      switch (right)
        {
        case 'l':
          return LOOKUP;
        case 'r':
          return READ;
        case 's':
          return KEEP_SEEN;
        case 'w':
          return WRITE;
        case 'i':
          return INSERT;
        case 'p':
          return POST;
        case 'c':
          return CREATE;
        case 'd':
          return DELETE;
        case 'a':
          return ADMINISTER;
        default:
          throw new IllegalArgumentException();
        }
    }

    public String toString()
    {
      switch (code)
        {
        case IMAPConstants.RIGHTS_LOOKUP:
          return "l";
        case IMAPConstants.RIGHTS_READ:
          return "r";
        case IMAPConstants.RIGHTS_SEEN:
          return "s";
        case IMAPConstants.RIGHTS_WRITE:
          return "w";
        case IMAPConstants.RIGHTS_INSERT:
          return "i";
        case IMAPConstants.RIGHTS_POST:
          return "p";
        case IMAPConstants.RIGHTS_CREATE:
          return "c";
        case IMAPConstants.RIGHTS_DELETE:
          return "d";
        case IMAPConstants.RIGHTS_ADMIN:
          return "a";
        default:
          throw new IllegalStateException();
        }
    }
    
  }

  int rights;

  public Rights()
  {
  }

  public Rights(Rights rights)
  {
    this.rights = rights.rights;
  }

  public Rights(String rights)
  {
    if (rights != null)
      {
        int len = rights.length();
        for (int i = 0; i < len; i++)
          this.rights |= Right.getInstance(rights.charAt(i)).code;
      }
  }

  public Rights(Right right)
  {
    this.rights = right.code;
  }

  public void add(Rights.Right right)
  {
    this.rights |= right.code;
  }

  public void add(Rights rights)
  {
    this.rights |= rights.rights;
  }

  public void remove(Rights.Right right)
  {
    this.rights -= right.code;
  }

  public void remove(Rights rights)
  {
    this.rights -= rights.rights;
  }

  public boolean contains(Rights.Right right)
  {
    return (this.rights & right.code) > 0;
  }

  public boolean contains(Rights rights)
  {
    return (this.rights & rights.rights) > 0;
  }

  public boolean equals(Object obj)
  {
    return (obj instanceof Rights && ((Rights) obj).rights == this.rights);
  }

  public int hashCode()
  {
    return rights;
  }

}

