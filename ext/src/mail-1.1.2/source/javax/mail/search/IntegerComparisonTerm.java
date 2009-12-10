/*
 * IntegerComparisonTerm.java
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
 * An integer comparison.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public abstract class IntegerComparisonTerm
  extends ComparisonTerm
{

  /**
   * The number.
   */
  protected int number;

  protected IntegerComparisonTerm(int comparison, int number)
  {
    this.comparison = comparison;
    this.number = number;
  }

  /**
   * Returns the number to compare with.
   */
  public int getNumber()
  {
    return number;
  }

  /** 
   * Returns the type of comparison.
   */
  public int getComparison()
  {
    return super.comparison;
  }

  protected boolean match(int i)
  {
    switch (comparison)
      {
      case LE:
        return i <= number;
      case LT:
        return i < number;
      case EQ:
        return i == number;
      case NE:
        return i != number;
      case GT:
        return i > number;
      case GE:
        return i >= number;
      }
    return false;
  }

  public boolean equals(Object other)
  {
    return (other instanceof IntegerComparisonTerm &&
       ((IntegerComparisonTerm) other).number == number &&
        super.equals(other));
  }

  public int hashCode()
  {
    return number + super.hashCode();
  }
  
}

