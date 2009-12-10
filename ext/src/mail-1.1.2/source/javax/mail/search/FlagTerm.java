/*
 * FlagTerm.java
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

import javax.mail.Flags;
import javax.mail.Message;

/**
 * A comparison of message flags.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public final class FlagTerm
  extends SearchTerm
{

  /**
   * Indicates whether to test for the presence or absence of the specified
   * flag. If true, test whether all the specified flags are present, 
   * otherwise test whether all the specified flags are absent.
   */
  protected boolean set;

  /**
   * The flags to test.
   */
  protected Flags flags;

  /**
   * Constructor.
   * @param flags the flags to test
   * @param set whether to test for presence or absence of the specified
   * flags
   */
  public FlagTerm(Flags flags, boolean set)
  {
    this.flags = flags;
    this.set = set;
  }

  /**
   * Returns the flags to test.
   */
  public Flags getFlags()
  {
    return (Flags) flags.clone();
  }

  /**
   * Indicates whether to test for the presence or the absence of the
   * specified flags.
   */
  public boolean getTestSet()
  {
    return set;
  }

  /**
   * Returns true if the flags in the specified message match this term.
   */
  public boolean match(Message msg)
  {
    try
      {
        Flags messageFlags = msg.getFlags();
        if (set)
          {
            return messageFlags.contains(flags);
          }
        Flags.Flag[] systemFlags = flags.getSystemFlags();
        for (int i = 0; i < systemFlags.length; i++)
          {
            if (messageFlags.contains(systemFlags[i]))
              {
                return false;
              }
          }
        String[] userFlags = flags.getUserFlags();
        for (int i = 0; i < userFlags.length; i++)
          {
            if (messageFlags.contains(userFlags[i]))
              {
                return false;
              }
          }
        return true;
      }
    catch (Exception e)
      {
      }
    return false;
  }

  public boolean equals(Object other)
  {
    if (other instanceof FlagTerm)
      {
        FlagTerm ft = (FlagTerm) other;
        return (ft.set == set && ft.flags.equals(flags));
      }
    return false;
  }

  public int hashCode()
  {
    return set ?  flags.hashCode() : ~flags.hashCode();
  }
  
}

