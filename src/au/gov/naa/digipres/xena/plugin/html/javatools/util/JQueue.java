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
 *  Implements a regular queue. The only unusual thing is you can delete things
 *  from the middle of the queue efficiently by supplying the QueueKey which is
 *  returned by put(). In terms of implementation we use a Dictionary Hashtable
 *  instead of a perhaps more traditional vector with round-robin pointers. The
 *  benefit here is efficient implementation of the remove functionality. It
 *  also happens to be a really easy way to implement queues in general. The way
 *  it works is that the dictionary is used as a sparse array. The upper and
 *  lower bounds of the sparse array keeping growing up and up towards infinity.
 *  This is ok because the key is a long and will take several million years of
 *  running before any problems arise.
 *
 * @created    December 13, 2001
 */
public class JQueue {
	/**
	 *  We store the queue here
	 */
	private Dictionary hash;

	/**
	 *  The next "put" position.
	 */
	private long put;
	/**
	 *  The next "get" position
	 */
	private long get;

	/**
	 *  Constructor which takes an initial guesstimate of how many items we expect
	 *  to store in it
	 *
	 * @param  size  Description of Parameter
	 */
	public JQueue(int size) {
		init(size);
	}

	public JQueue() {
		init(10);
	}

	/**
	 *  Pull something out of the end of the queue and remove it from the queue.
	 *
	 * @return    Description of the Returned Value
	 */
	public Object get() {
		Object rtn = peek();
		hash.remove(new Long(get));
		// allow garbage collection
		get++;
		return rtn;
	}

	/**
	 *  The number of elements in the queue
	 *
	 * @return    Description of the Returned Value
	 */
	public int size() {
		return hash.size();
	}

	/**
	 *  Remove an item from anywhere in the queue.
	 *
	 * @param  key  Description of Parameter
	 * @return      The object that was removed.
	 * @key         The key which was returned from put().
	 * @see         Queue#put(Object)
	 */
	public Object remove(QueueKey key) {
		Long k = new Long(key.v);
		Object rtn = hash.get(k);
		hash.remove(k);
		return rtn;
	}

	/**
	 *  Push something onto the queue
	 *
	 * @param  o  Description of Parameter
	 * @return    An external key that can be passed to remove() to pull it from
	 *      the queue. The value of the key should be regarded as meaningless to
	 *      external parties.
	 * @see       Queue#remove(QueueKey)
	 */
	public QueueKey put(Object o) {
		QueueKey rtn = null;
		hash.put(new Long(put), o);
		rtn = new QueueKey(put);
		put++;
		return rtn;
	}

	/**
	 *  Is the queue empty?
	 *
	 * @return    Description of the Returned Value
	 */
	public boolean empty() {
		return hash.size() == 0;
	}

	/**
	 *  Get something out of the queue without removing it.
	 *
	 * @return    Description of the Returned Value
	 */
	public Object peek() {
		Object rtn = null;
		if (empty()) {
			throw new NoSuchElementException("Queue Empty");
		}
		while (rtn == null) {
			rtn = hash.get(new Long(get));
			if (rtn == null) {
				get++;
			}
		}
		return rtn;
	}

	/**
	 *  Setup initialization
	 *
	 * @param  size  Description of Parameter
	 */
	private void init(int size) {
		hash = new Hashtable(size);
		put = 0;
		get = 0;
	}

	/**
	 *  A class which can be used to pass to remove to remove something from the
	 *  middle of the queue.
	 *
	 * @created    December 13, 2001
	 */
	public class QueueKey {
		/**
		 *  A key for the dictionary
		 */
		long v;

		QueueKey(long key) {
			v = key;
		}
	}
}
