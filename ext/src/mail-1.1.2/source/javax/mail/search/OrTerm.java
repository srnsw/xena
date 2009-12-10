/*
 * OrTerm.java
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
 * A logical OR of a number of search terms.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public final class OrTerm
  extends SearchTerm
{

  /**
   * The target terms.
   */
  protected SearchTerm[] terms;

  /**
   * Constructor with two operands.
   * @param t1 the first term
   * @param t2 the second term
   */
  public OrTerm(SearchTerm t1, SearchTerm t2)
  {
    terms = new SearchTerm[2];
    terms[0] = t1;
    terms[1] = t2;
  }

  /**
   * Constructor with multiple search terms.
   * @param t the terms
   */
  public OrTerm(SearchTerm[] t)
  {
    terms = new SearchTerm[t.length];
    System.arraycopy(t, 0, terms, 0, t.length);
  }

  /**
   * Returns the search terms.
   */
  public SearchTerm[] getTerms()
  {
    return (SearchTerm[]) terms.clone();
  }

  /**
   * Returns true only if any of the terms specified in this term match
   * the given message.
   */
  public boolean match(Message msg)
  {
    for (int i = 0; i < terms.length; i++)
      {
        if (terms[i].match(msg))
          {
            return true;
          }
      }
    return false;
  }

  public boolean equals(Object other)
  {
    if (other instanceof OrTerm)
      {
        OrTerm orterm = (OrTerm) other;
        if (orterm.terms.length != terms.length)
          {
            return false;
          }
        for (int i = 0; i < terms.length; i++)
          {
            if (!terms[i].equals(orterm.terms[i]))
              {
                return false;
              }
          }
        return true;
      }
    return false;
  }

  public int hashCode()
  {
    int acc = 0;
    for (int i = 0; i < terms.length; i++)
      {
        acc += terms[i].hashCode();
      }
    return acc;
  }
  
}

