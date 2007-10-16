/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

import java.util.*;

/**
 *  General purpose cache class. Acts basically like a weak reference dictionary
 *  except that it also returns null when asked to find an item whose use-by
 *  date has expired. It works with two main data structures: A Dictionary which
 *  provides fast access into the cache, and a Queue which keeps track of the
 *  least recently used items in the cache so that old items can be removed and
 *  limit the cache to a bounded size.
 *
 * @created    December 13, 2001
 */

public class Cache {
	/**
	 *  The fast access dictionary
	 */
	private Dictionary dictionary;

	/**
	 *  The lru (least recently used) queue
	 */
	private JQueue queue;

	/**
	 *  The maximum number of objects in the cache
	 */
	private int cacheMaxObj = 100;

	/**
	 *  The maximum age of items in the cache in milliseconds.
	 */
	private int cacheMaxAge = -1;

	public Cache() {
		dictionary = new Hashtable();
		queue = new JQueue();
	}

	/**
	 *  Constructor which takes an initial guesstimate of how many items we expect
	 *  to store in it
	 *
	 * @param  size  Description of Parameter
	 */
	public Cache(int size) {
		dictionary = new Hashtable(size);
		queue = new JQueue(size);
	}

	/**
	 *  Set the maximum number of objects to cache.
	 *
	 * @param  n  The new cacheMaxObj value
	 * @n         the number of objects to cache.
	 */
	public void setCacheMaxObj(int n) {
		cacheMaxObj = n;
	}

	/**
	 *  Change the maximum age of items in the cache. Can be changed at any time.
	 *
	 * @param  s  The new cacheMaxAgeSeconds value
	 * @s         maximum age in seconds
	 */
	public void setCacheMaxAgeSeconds(int s) {
		cacheMaxAge = s * 1000;
	}

	/**
	 *  Turn off cache expiry based on age
	 */
	public void setNoCacheMaxAge() {
		cacheMaxAge = -1;
	}

	/**
	 *  Get an object from the cache. Returns null if the object is not in the
	 *  cache.
	 *
	 * @param  key  Description of Parameter
	 * @return      Description of the Returned Value
	 * @key         The key to use to retrieve the object
	 */
	public Object get(Object key) {
		CacheItem result = (CacheItem) dictionary.get(key);
		Object rtn = null;
		if (result != null) {
			Date now = null;
			Date expiration = null;
			if (0 <= cacheMaxAge) {
				now = new Date();
				expiration = new Date(result.time.getTime() + cacheMaxAge);
			}
			if (now == null || expiration.after(now)) {
				// We just used it, so reset the date.
				result.time = new Date();
				// Since we just used this item
				// put it at the front of the lru queue.
				queue.remove(result.qkey);
				result.qkey = queue.put(result);
				rtn = result.item;
			} else {
				// It's out of date. Might as well throw it away.
				// (It would be removed later anyway when it comes
				// through the queue, but better to do it now).
				queue.remove(result.qkey);
				dictionary.remove(key);
			}
		}
		return rtn;
	}

	/**
	 *  Put a key value pair into the cache.
	 *
	 * @param  key   Description of Parameter
	 * @param  item  Description of Parameter
	 * @key          The key
	 * @item         The value
	 */
	public void put(Object key, Object item) {
		CacheItem cacheItem = new CacheItem(key, item);
		cacheItem.setQueueKey(queue.put(cacheItem));
		dictionary.put(key, cacheItem);
		if (cacheMaxObj < queue.size()) {
			// Don't let the cache grow beyond our limit.
			CacheItem obj = (CacheItem) queue.get();
			dictionary.remove(obj.key);
		}
	}

	/**
	 *  Expire the item in the cache with the given key.
	 *
	 * @param  key  Description of Parameter
	 * @key         The key of the object to expire from the cache.
	 */
	public void expire(Object key) {
		CacheItem result = (CacheItem) dictionary.get(key);
		if (result != null) {
			queue.remove(result.qkey);
			dictionary.remove(key);
		}
	}

	/**
	 *  This is the class we store in our internal data structures.
	 *
	 * @created    December 13, 2001
	 */
	class CacheItem {
		/**
		 *  The time this object was last accessed.
		 */
		Date time;

		/**
		 *  The key
		 */
		Object key;

		/**
		 *  The value
		 */
		Object item;
		JQueue.QueueKey qkey;

		public CacheItem(Object theKey, Object theItem) {
			key = theKey;
			item = theItem;
			time = new Date();
			// now
		}

		@Override
        public boolean equals(Object other) {
			return key.equals(((CacheItem) other).key);
		}

		@Override
        public int hashCode() {
			return key.hashCode();
		}

		void setQueueKey(JQueue.QueueKey q) {
			qkey = q;
		}
	}
}
