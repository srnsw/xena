/*
 * Flags.java
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

package javax.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The set of flags on a message.
 * Flags are composed of predefined system flags (Flags.Flag),
 * and user defined flags (case-independent String).
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class Flags
  implements Cloneable, Serializable
{

  /**
   * An individual system flag.
   */
  public static final class Flag
  {

    /*
     * This holds the reverse mappings for flag bits to
     * flag objects.
     * It is used internally by the Flags class.
     */
    private static final HashMap flag2flag = new HashMap(7);
    
    /**
     * This message has been answered.
     */
    public static final Flag ANSWERED = new Flag(0x00000001);

    /**
     * This message is marked deleted.
     */
    public static final Flag DELETED = new Flag(0x00000002);

    /**
     * This message is a draft.
     */
    public static final Flag DRAFT = new Flag(0x00000004);

    /**
     * This message is flagged.
     */
    public static final Flag FLAGGED = new Flag(0x00000008);

    /**
     * This message is recent.
     */
    public static final Flag RECENT = new Flag(0x00000010);

    /**
     * This message is seen.
     */
    public static final Flag SEEN = new Flag(0x00000020);

    /**
     * Special flag that indicates whether this folder supports 
     * user defined flags.
     */
    public static final Flag USER = new Flag(0x80000000);

    int flag;

    /*
     * Constructor.
     */
    private Flag(int flag)
    {
      this.flag = flag;
      flag2flag.put(new Integer(flag), this);
    }
    
  }

  private int systemFlags;

  private HashMap userFlags;
  
  /**
   * Construct an empty Flags object.
   */
  public Flags()
  {
    systemFlags = 0;
    userFlags = null;
  }

  /**
   * Construct a Flags object containing the given flags.
   */
  public Flags(Flags flags)
  {
    systemFlags = flags.systemFlags;
    if (flags.userFlags != null)
      {
        userFlags = (HashMap) flags.userFlags.clone();
      }
    else
      {
        userFlags = null;
      }
  }

  /**
   * Construct a Flags object containing the given system flag.
   */
  public Flags(Flag flag)
  {
    systemFlags = systemFlags | flag.flag;
    userFlags = null;
  }

  /**
   * Construct a Flags object containing the given user flag.
   */
  public Flags(String flag)
  {
    systemFlags = 0;
    userFlags = new HashMap(1);
    userFlags.put(flag.toLowerCase(), flag);
  }

  /**
   * Add the specified system flag.
   */
  public void add(Flag flag)
  {
    systemFlags = systemFlags | flag.flag;
  }

  /**
   * Add the specified user flag.
   */
  public void add(String flag)
  {
    if (userFlags == null)
      {
        userFlags = new HashMap(1);
      }
    synchronized (userFlags)
      {
        userFlags.put(flag.toLowerCase(), flag);
      }
  }

  /**
   * Add all the flags from the specified Flags object.
   */
  public void add(Flags flags)
  {
    systemFlags = systemFlags | flags.systemFlags;
    if (flags.userFlags != null)
      {
        synchronized (flags.userFlags)
          {
            if (userFlags == null)
              {
                userFlags = new HashMap(flags.userFlags);
              }
            else
              {
                synchronized (userFlags)
                  {
                    userFlags.putAll(flags.userFlags);
                  }
              }
          }
      }
  }

  /**
   * Remove the specified system flag.
   */
  public void remove(Flag flag)
  {
    systemFlags = systemFlags & ~flag.flag;
  }

  /**
   * Remove the specified user flag.
   */
  public void remove(String flag)
  {
    if (userFlags != null)
      {
        synchronized (userFlags)
          {
            userFlags.remove(flag.toLowerCase());
          }
      }
  }

  /**
   * Remove all flags in the given Flags object from this Flags object.
   */
  public void remove(Flags flags)
  {
    systemFlags = systemFlags & ~flags.systemFlags;
    if (userFlags != null && flags.userFlags != null)
      {
        synchronized (flags.userFlags)
          {
            synchronized (userFlags)
              {
                for (Iterator i = flags.userFlags.keySet().iterator();
                     i.hasNext(); )
                  {
                    userFlags.remove(i.next());
                  }
              }
          }
      }
  }

  /**
   * Indicates whether the specified system flag is set.
   */
  public boolean contains(Flag flag)
  {
    return (systemFlags & flag.flag) != 0;
  }

  /**
   * Indicates whether the specified user flag is set.
   */
  public boolean contains(String flag)
  {
    if (userFlags == null)
      {
        return false;
      }
    return userFlags.containsKey(flag.toLowerCase());
  }

  /**
   * Indicates whether all the flags in the specified Flags object 
   * are set in this Flags object.
   */
  public boolean contains(Flags flags)
  {
    if ((systemFlags & flags.systemFlags) == 0)
      {
        return false;
      }
    if (flags.userFlags != null)
      {
        if (userFlags == null)
          {
            return false;
          }
        synchronized (userFlags)
          {
            String[] fuf = flags.getUserFlags();
            for (int i = 0; i < fuf.length; i++)
              {
                if (!userFlags.containsKey(fuf[i].toLowerCase()))
                  {
                    return false;
                  }
              }
          }
    }
    return true;
  }

  public boolean equals(Object other)
  {
    if (other == this)
      {
        return true;
      }
    if (!(other instanceof Flags))
      {
        return false;
      }
    Flags flags = (Flags) other;
    if (flags.systemFlags != systemFlags)
      {
        return false;
      }
    if (flags.userFlags == null && userFlags == null)
      {
        return true;
      }
    return (flags.userFlags != null && userFlags != null &&
            flags.userFlags.equals(userFlags));
  }

  public int hashCode()
  {
    int hashCode = systemFlags;
    if (userFlags != null)
      {
        hashCode += userFlags.hashCode();
      }
    return hashCode;
  }

  /**
   * Returns the system flags.
   */
  public Flag[] getSystemFlags()
  {
    ArrayList acc = new ArrayList(7);
    for (Iterator i = Flag.flag2flag.keySet().iterator(); i.hasNext(); )
      {
        Integer flag = (Integer) i.next();
        if ((systemFlags & flag.intValue()) != 0)
          {
            acc.add(Flag.flag2flag.get(flag));
          }
      }
    Flag[] f = new Flag[acc.size()];
    acc.toArray(f);
    return f;
  }

  /**
   * Returns the user flags.
   */    
  public String[] getUserFlags()
  {
    if (userFlags == null)
      {
        return new String[0];
      }
    else
      {
        synchronized (userFlags)
          {
            String[] f = new String[userFlags.size()];
            int index = 0;
            for (Iterator i = userFlags.keySet().iterator(); i.hasNext(); )
              {
                f[index++] = (String) userFlags.get(i.next());
              }
            return f;
          }
      }
  }

  public Object clone()
  {
    return new Flags(this);
  }

}
