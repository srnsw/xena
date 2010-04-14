/*
 * ContentType.java
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
 * A MIME Content-Type value.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class ContentType
{

  /*
   * The primary type.
   */
  private String primaryType;

  /*
   * The subtype.
   */
  private String subType;

  /*
   * The parameters.
   */
  private ParameterList list;

  /**
   * Constructor for an empty Content-Type.
   */
  public ContentType()
  {
  }

  /**
   * Constructor.
   * @param primaryType the primary type
   * @param subType the subtype
   * @param list the parameters
   */
  public ContentType(String primaryType, String subType, ParameterList list)
  {
    this.primaryType = primaryType;
    this.subType = subType;
    this.list = list;
  }

  /**
   * Constructor that parses a Content-Type value from an RFC 2045 string
   * representation.
   * @param s the Content-Type value
   * @exception ParseException if an error occurred during parsing
   */
  public ContentType(String s)
    throws ParseException
  {
    HeaderTokenizer ht = new HeaderTokenizer(s, HeaderTokenizer.MIME);
    HeaderTokenizer.Token token = ht.next();
    if (token.getType() != HeaderTokenizer.Token.ATOM)
      {
        throw new ParseException("expected primary type: " + s);
      }
    primaryType = token.getValue();
    token = ht.next();
    if (token.getType() != 0x2f) // '/'
      {
        throw new ParseException("expected '/': " + s);
      }
    token = ht.next();
    if (token.getType() != HeaderTokenizer.Token.ATOM)
      {
        throw new ParseException("expected subtype: " + s);
      }
    subType = token.getValue();
    s = ht.getRemainder();
    if (s != null)
      {
        list = new ParameterList(s);
      }
  }

  /**
   * Returns the primary type.
   */
  public String getPrimaryType()
  {
    return primaryType;
  }

  /**
   * Returns the subtype.
   */
  public String getSubType()
  {
    return subType;
  }

  /**
   * Returns the MIME type string, without the parameters.
   */
  public String getBaseType()
  {
    if (primaryType == null || subType == null)
      {
        return null;
      }
    StringBuffer buffer = new StringBuffer();
    buffer.append(primaryType);
    buffer.append('/');
    buffer.append(subType);
    return buffer.toString();
  }

  /**
   * Returns the specified parameter value.
   */
  public String getParameter(String name)
  {
    return (list == null) ? null : list.get(name);
  }

  /**
   * Returns the parameters.
   */
  public ParameterList getParameterList()
  {
    return list;
  }

  /**
   * Sets the primary type.
   */
  public void setPrimaryType(String primaryType)
  {
    this.primaryType = primaryType;
  }

  /**
   * Sets the subtype.
   */
  public void setSubType(String subType)
  {
    this.subType = subType;
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
   * @param list the Parameter list
   */
  public void setParameterList(ParameterList list)
  {
    this.list = list;
  }

  /**
   * Returns an RFC 2045 string representation of this Content-Type.
   */
  public String toString()
  {
    if (primaryType == null || subType == null)
      {
        return null;
      }
    
    StringBuffer buffer = new StringBuffer();
    buffer.append(primaryType);
    buffer.append('/');
    buffer.append(subType);
    if (list != null)
      {
        // Add the parameters, using the toString(int) method
        // which allows the resulting string to fold properly onto the next
        // header line.
        int used = buffer.length() + 14; // "Content-Type: ".length()
        buffer.append(list.toString(used));
      }
    return buffer.toString();
  }

  /**
   * Returns true if the specified Content-Type matches this Content-Type.
   * Parameters are ignored.
   * <p>
   * If the subtype of either Content-Type is the special character '*',
   * the subtype is ignored during the match.
   * @param cType the Content-Type for comparison
   */
  public boolean match(ContentType cType)
  {
    if (!primaryType.equalsIgnoreCase(cType.getPrimaryType()))
      {
        return false;
      }
    String cTypeSubType = cType.getSubType();
    if (subType.charAt(0) == '*' || cTypeSubType.charAt(0) == '*')
      {
        return true;
      }
    return subType.equalsIgnoreCase(cTypeSubType);
  }

  /**
   * Returns true if the specified Content-Type matches this Content-Type.
   * Parameters are ignored.
   * <p>
   * If the subtype of either Content-Type is the special character '*',
   * the subtype is ignored during the match.
   * @param s the RFC 2045 string representation of the Content-Type to match
   */
  public boolean match(String s)
  {
    try
      {
        return match(new ContentType(s));
      }
    catch (ParseException e)
      {
        return false;
      }
  }
  
}

