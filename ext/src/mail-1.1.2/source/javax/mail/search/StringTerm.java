/*
 * StringTerm.java
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

/**
 * A comparison of string values.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class StringTerm
  extends SearchTerm
{

  /**
   * The pattern to match.
   */
  protected String pattern;

  /**
   * Whether to ignore case during comparison.
   */
  protected boolean ignoreCase;

  protected StringTerm(String pattern)
  {
    this(pattern, true);
  }

  protected StringTerm(String pattern, boolean ignoreCase)
  {
    this.pattern = pattern;
    this.ignoreCase = ignoreCase;
  }

  /**
   * Returns the pattern to match.
   */
  public String getPattern()
  {
    return pattern;
  }

  /**
   * Indicates whether to ignore case during comparison.
   */
  public boolean getIgnoreCase()
  {
    return ignoreCase;
  }

  /**
   * Returns true if the specified pattern is a substring of the given string.
   */
  protected boolean match(String s)
  {
    int patlen = pattern.length();
    int len = s.length() - patlen;
    for (int i = 0; i <= len; i++)
      {
        if (s.regionMatches(ignoreCase, i, pattern, 0, patlen))
          {
            return true;
          }
      }
    return false;
  }

  public boolean equals(Object other)
  {
    if (other instanceof StringTerm)
      {
        StringTerm st = (StringTerm)other;
        if (ignoreCase)
          {
            return st.pattern.equalsIgnoreCase(pattern) && 
              st.ignoreCase == ignoreCase;
          }
        else
          {
            return st.pattern.equals(pattern) && 
              st.ignoreCase == ignoreCase;
          }
      }
    return false;
  }

  public int hashCode()
  {
    return (ignoreCase) ? pattern.hashCode() : ~pattern.hashCode();
  }
  
}

