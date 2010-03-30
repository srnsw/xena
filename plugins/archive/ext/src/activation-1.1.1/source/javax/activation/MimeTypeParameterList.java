/*
 * MimeTypeParameterList.java
 * Copyright (C) 2004 The Free Software Foundation
 * 
 * This file is part of GNU Java Activation Framework (JAF), a library.
 * 
 * GNU JAF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JAF is distributed in the hope that it will be useful,
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
package javax.activation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A list of MIME type parameters, as specified in RFCs 2045 and 2046.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class MimeTypeParameterList
{

  private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";
  
  private List parameterNames;
  private Map parameterValues;
  
  /**
   * Constructor for an empty parameter list.
   */
  public MimeTypeParameterList()
  {
    parameterNames = new ArrayList();
    parameterValues = new HashMap();
  }

  /**
   * Constructor that parses the specified MIME parameter data.
   * @param parameterList a MIME parameter list string representation
   */
  public MimeTypeParameterList(String parameterList)
    throws MimeTypeParseException
  {
    parameterNames = new ArrayList();
    parameterValues = new HashMap();
    parse(parameterList);
  }

  /**
   * Parses the specified MIME parameter data, storing the results in this
   * object.
   * @param parameterList a MIME parameter list string representation
   */
  protected void parse(String parameterList)
    throws MimeTypeParseException
  {
    if (parameterList == null)
      {
        return;
      }
    // Tokenize list into parameters
    char[] chars = parameterList.toCharArray();
    int len = chars.length;
    boolean inQuotedString = false;
    StringBuffer buffer = new StringBuffer();
    List params = new ArrayList();
    for (int i = 0; i < len; i++)
      {
        char c = chars[i];
        if (c == ';' && !inQuotedString)
          {
            String param = buffer.toString().trim();
            if (param.length() > 0)
              {
                params.add(param);
              }
            buffer.setLength(0);
          }
        else
          {
            if (c == '"')
              {
                inQuotedString = !inQuotedString;
              }
            buffer.append(c);
          }
      }
    String param = buffer.toString().trim();
    if (param.length() > 0)
      {
        params.add(param);
      }
    
    // Tokenize each parameter into name + value
    for (Iterator i = params.iterator(); i.hasNext(); )
      {
        param = (String)i.next();
        int ei = param.indexOf('=');
        if (ei == -1)
          {
            throw new MimeTypeParseException("Couldn't find the '=' that " +
                                             "separates a parameter name " +
                                             "from its value.");
          }
        String name = param.substring(0, ei).trim();
        MimeType.checkValidity(name, "Parameter name is invalid");
        String value = param.substring(ei + 1).trim();
        len = value.length();
        if (len > 1 && value.charAt(0) == '"' &&
            value.charAt(len - 1) == '"')
          {
            value = unquote(value);
          }
        else
          {
            MimeType.checkValidity(name, "Parameter value is invalid");
          }
        
        parameterNames.add(name);
        parameterValues.put(name.toLowerCase(), value);
      }
  }
  
  /**
   * Returns the number of parameters.
   */
  public synchronized int size()
  {
    return parameterNames.size();
  }
  
  /**
   * Indicates if there are no parameters.
   */
  public synchronized boolean isEmpty()
  {
    return parameterNames.isEmpty();
  }

  /**
   * Returns the value for the specified parameter name.
   * @param name the parameter name
   */
  public synchronized String get(String name)
  {
    name = name.trim();
    return (String) parameterValues.get(name.toLowerCase());
  }
  
  /**
   * Sets the value for the specified parameter name.
   * @param name the parameter name
   * @param value the parameter value
   */
  public synchronized void set(String name, String value)
  {
    name = name.trim();
    boolean exists = false;
    for (Iterator i = parameterNames.iterator(); i.hasNext(); )
      {
        String pname = (String)i.next();
        if (name.equalsIgnoreCase(pname))
          {
            exists = true;
          }
      }
    if (!exists)
      {
        parameterNames.add(name);
      }
    parameterValues.put(name.toLowerCase(), value);
  }
  
  /**
   * Removes the parameter identified by the specified name.
   * @param name the parameter name
   */
  public synchronized void remove(String name)
  {
    name = name.trim();
    for (Iterator i = parameterNames.iterator(); i.hasNext(); )
      {
        String pname = (String)i.next();
        if (name.equalsIgnoreCase(pname))
          {
            i.remove();
          }
      }
    parameterValues.remove(name.toLowerCase());
  }
  
  /**
   * Returns an enumeration of all the parameter names.
   */
  public synchronized Enumeration getNames()
  {
    return new IteratorEnumeration(parameterNames.iterator());
  }
  
  /**
   * Returns an RFC 2045-compliant string representation of this parameter
   * list.
   */
  public synchronized String toString()
  {
    StringBuffer buffer = new StringBuffer();
    for (Iterator i = parameterNames.iterator(); i.hasNext(); )
      {
        String name = (String)i.next();
        String value = (String)parameterValues.get(name.toLowerCase());
        
        buffer.append(';');
        buffer.append(' ');
        buffer.append(name);
        buffer.append('=');
        buffer.append(quote(value));
      }
    return buffer.toString();
  }
  
  private static String quote(String value)
  {
    boolean needsQuoting = false;
    int len = value.length();
    for (int i = 0; i < len; i++)
      {
        if (!MimeType.isValidChar(value.charAt(i)))
          {
            needsQuoting = true;
            break;
          }
      }
    
    if (needsQuoting)
      {
        StringBuffer buffer = new StringBuffer();
        buffer.append('"');
        for (int i = 0; i < len; i++)
          {
            char c = value.charAt(i);
            if (c == '\\' || c == '"')
              {
                buffer.append('\\');
              }
            buffer.append(c);
          }
        buffer.append('"');
        return buffer.toString();
      }
    return value;
  }
  
  private static String unquote(String value)
  {
    int len = value.length();
    StringBuffer buffer = new StringBuffer();
    for (int i = 1; i < len - 1; i++)
      {
        char c = value.charAt(i);
        if (c == '\\')
          {
            i++;
            if (i < len - 1)
              {
                c = value.charAt(i);
                if (c != '\\' && c != '"')
                  {
                    buffer.append('\\');
                  }
              }
          }
        buffer.append(c);
      }
    return buffer.toString();
  }
  
  /**
   * Enumeration proxy for an Iterator.
   */
  static class IteratorEnumeration
    implements Enumeration
  {
    
    final Iterator iterator;
    
    IteratorEnumeration(Iterator iterator)
    {
      this.iterator = iterator;
    }
    
    public boolean hasMoreElements()
    {
      return iterator.hasNext();
    }
    
    public Object nextElement()
    {
      return iterator.next();
    }
    
  }
  
}

