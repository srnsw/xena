/*
 * IMAPFlags.java
 * Copyright(C) 2003 Chris Burdess <dog@gnu.org>
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.Flags;

import gnu.inet.imap.IMAPConstants;

/**
 * A Flags implementation that can provide delta flag changes for an IMAP
 * implementation.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class IMAPFlags
  extends Flags
{

  Flags saved;

  /**
   * Save the state of this flags object.
   */
  void checkpoint()
  {
    saved = new Flags(this);
  }

  /**
   * Returns the flags added since the last checkpoint.
   */
  List getAddedFlags() {
    if (saved == null)
      {
        return Collections.EMPTY_LIST;
      }
    List current = getIMAPFlags(this);
    List original = getIMAPFlags(saved);
    current.removeAll(original);
    return current;
  }

  /**
   * Returns the flags removed since the last checkpoint.
   */
  List getRemovedFlags() {
    if (saved == null)
      {
        return Collections.EMPTY_LIST;
      }
    List current = getIMAPFlags(this);
    List original = getIMAPFlags(saved);
    original.removeAll(current);
    return original;
  }

  /**
   * Returns a list of IMAP flags for the given Flags object.
   */
  static List getIMAPFlags(Flags flags) {
    Flags.Flag[] sflags = flags.getSystemFlags();
    String[] uflags = flags.getUserFlags();
    List iflags = new ArrayList(sflags.length + uflags.length);
    for (int i = 0; i < sflags.length; i++)
      {
        Flags.Flag f = sflags[i];
        if (f == Flags.Flag.ANSWERED)
          {
            iflags.add(IMAPConstants.FLAG_ANSWERED);
          }
        else if (f == Flags.Flag.DELETED)
          {
            iflags.add(IMAPConstants.FLAG_DELETED);
          }
        else if (f == Flags.Flag.DRAFT)
          {
            iflags.add(IMAPConstants.FLAG_DRAFT);
          }
        else if (f == Flags.Flag.FLAGGED)
          {
            iflags.add(IMAPConstants.FLAG_FLAGGED);
          }
        else if (f == Flags.Flag.SEEN)
          {
            iflags.add(IMAPConstants.FLAG_SEEN);
          }
      }
    iflags.addAll(Arrays.asList(uflags));
    return iflags;
  }

}

