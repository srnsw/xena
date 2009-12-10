/*
 * Quota.java
 * Copyright (C) 2005 The Free Software Foundation
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

/**
 * A set of quotas for a given quota root.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @see RFC 2087
 * @version 1.4
 * @since JavaMail 1.4
 */
public class Quota
{

  /**
   * An individual quota resource.
   * @since JavaMail 1.4
   */
  public static class Resource
  {

    /**
     * The resource name.
     */
    public String name;

    /**
     * The current resource usage.
     */
    public long usage;

    /**
     * The usage limit for the resource.
     */
    public long limit;

    /**
     * Constructor.
     * @param name the resource name
     * @param usage the current usage
     * @param limit the usage limit
     */
    public Resource(String name, long usage, long limit)
    {
      this.name = name;
      this.usage = usage;
      this.limit = limit;
    }
    
  }

  /**
   * The quota root.
   */
  public String quotaRoot;

  /**
   * The resources associated with this quota.
   */
  public Resource[] resources;

  /**
   * Constructor.
   * @param quotaRoot the quota root
   */
  public Quota(String quotaRoot)
  {
    this.quotaRoot = quotaRoot;
  }

  /**
   * Sets a resource limit.
   * @param name the resource name
   * @param limit the usage limit
   */
  public void setResourceLimit(String name, long limit)
  {
    if (resources != null)
      {
        boolean found = false;
        for (int i = 0; i < resources.length; i++)
          {
            if (resources[i].name.equals(name))
              {
                resources[i].limit = limit;
                found = true;
              }
          }
        if (!found)
          {
            Resource[] r = new Resource[resources.length + 1];
            System.arraycopy(resources, 0, r, 0, resources.length);
            r[resources.length] = new Resource(name, 0L, limit);
            resources = r;
          }
      }
    else
      {
        resources = new Resource[1];
        resources[0] = new Resource(name, 0L, limit);
      }
  }
  
}
