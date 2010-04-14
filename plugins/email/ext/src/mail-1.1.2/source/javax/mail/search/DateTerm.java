/*
 * DateTerm.java
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

import java.util.Date;

/**
 * A date comparison.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class DateTerm
  extends ComparisonTerm
{

  /**
   * The date.
   */
  protected Date date;

  /**
   * Constructor.
   * @param comparison the comparison type
   * @param date the date
   */
  protected DateTerm(int comparison, Date date)
  {
    this.comparison = comparison;
    this.date = date;
  }

  /**
   * Returns the date to compare with.
   */
  public Date getDate()
  {
    return new Date(date.getTime());
  }

  /**
   * Returns the comparison operator.
   */
  public int getComparison()
  {
    return comparison;
  }

  /**
   * Returns true only if this comparison matches the supplied date.
   */
  protected boolean match(Date d)
  {
    switch (comparison)
      {
      case LE:
        return d.before(date) || d.equals(date);
      case LT:
        return d.before(date);
      case EQ:
        return d.equals(date);
      case NE:
        return !d.equals(date);
      case GT:
        return d.after(date);
      case GE:
        return d.after(date) || d.equals(date);
    }
    return false;
  }

  public boolean equals(Object other)
  {
    return ((other instanceof DateTerm) &&
           ((DateTerm)other).date.equals(date) &&
            super.equals(other));
  }

  public int hashCode()
  {
    return date.hashCode() + super.hashCode();
  }

}

