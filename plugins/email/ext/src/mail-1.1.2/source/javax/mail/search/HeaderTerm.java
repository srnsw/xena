/*
 * HeaderTerm.java
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

import javax.mail.Message;

/**
 * A case-insensitive string comparison of message header values.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public final class HeaderTerm
  extends StringTerm
{

  /**
   * The header name.
   */
  protected String headerName;

  /**
   * Constructor.
   * @param headerName the header name
   * @param pattern the pattern to search for
   */
  public HeaderTerm(String headerName, String pattern)
  {
    super(pattern);
    this.headerName = headerName;
  }

  /**
   * Returns the name of the header to compare with.
   */
  public String getHeaderName()
  {
    return headerName;
  }

  /**
   * Returns true if the header in the specified message matches the pattern
   * specified in this term.
   */
  public boolean match(Message msg)
  {
    try
      {
        String[] headers = msg.getHeader(headerName);
        if (headers != null)
          {
            for (int i = 0; i < headers.length; i++)
              {
                if (super.match(headers[i]))
                  {
                    return true;
                  }
              }
          }
      }
    catch (Exception e)
      {
      }
    return false;
  }

  public boolean equals(Object other)
  {
    if (other instanceof HeaderTerm)
      {
        HeaderTerm ht = (HeaderTerm) other;
        return ht.headerName.equalsIgnoreCase(headerName) &&
          super.equals(ht);
      }
    return false;
  }

  public int hashCode()
  {
    return headerName.toLowerCase().hashCode() + super.hashCode();
  }
  
}

