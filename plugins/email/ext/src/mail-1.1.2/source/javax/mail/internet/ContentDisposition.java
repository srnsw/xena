/*
 * ContentDisposition.java
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

package javax.mail.internet;

/**
 * A MIME Content-Disposition value.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class ContentDisposition
{

  /*
   * The disposition value.
   */
  private String disposition;

  /*
   * The parameters.
   */
  private ParameterList list;

  /**
   * Constructor for an empty Content-Disposition.
   */
  public ContentDisposition()
  {
  }

  /**
   * Constructor.
   * @param disposition the disposition value
   * @param list the parameters
   */
  public ContentDisposition(String disposition, ParameterList list)
  {
    this.disposition = disposition;
    this.list = list;
  }

  /**
   * Constructor that parses a Content-Disposition value from an RFC 2045
   * string representation.
   * @param s the Content-Disposition value
   * @exception ParseException if there was an error in the value
   */
  public ContentDisposition(String s)
    throws ParseException
  {
    HeaderTokenizer ht = new HeaderTokenizer(s, HeaderTokenizer.MIME);
    HeaderTokenizer.Token token = ht.next();
    if (token.getType() != HeaderTokenizer.Token.ATOM)
      {
        throw new ParseException();
      }
    
    disposition = token.getValue();
    
    s = ht.getRemainder();
    if (s != null)
      {
        list = new ParameterList(s);
      }
  }

  /**
   * Returns the disposition value.
   */
  public String getDisposition()
  {
    return disposition;
  }

  /**
   * Returns the specified parameter value, or <code>null</code> if this
   * parameter is not present.
   * @param name the parameter name
   */
  public String getParameter(String name)
  {
    return (list != null) ? list.get(name) : null;
  }

  /**
   * Returns the parameters, or <code>null</code> if there are no
   * parameters.
   */
  public ParameterList getParameterList()
  {
    return list;
  }

  /**
   * Sets the disposition value.
   * @param disposition the disposition value
   */
  public void setDisposition(String disposition)
  {
    this.disposition = disposition;
  }

  /**
   * Sets the specified parameter.
   * @param name the parameter name
   * @param value the parameter value
   */
  public void setParameter(String name, String value)
  {
    if (list == null)
      {
        list = new ParameterList();
      }
    list.set(name, value);
  }

  /**
   * Sets the parameters.
   * @param list the parameters
   */
  public void setParameterList(ParameterList list)
  {
    this.list = list;
  }

  /**
   * Returns an RFC 2045 string representation of this Content-Disposition.
   */
  public String toString()
  {
    if (disposition == null)
      {
        return null;
      }
    if (list == null)
      {
        return disposition;
      }
    else
      {
        StringBuffer buffer = new StringBuffer();
        buffer.append(disposition);
        
        // Add the parameters, using the toString(int) method
        // which allows the resulting string to fold properly onto the next
        // header line.
        int used = buffer.length() + 21; // "Content-Disposition: ".length()
        buffer.append(list.toString(used));
        return buffer.toString();
      }
  }
  
}

