
package com.softhub.ps.util;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.util.Hashtable;
import java.util.LinkedList;

public class Cache {

	private int size;
	private int maximumSize;
	private Hashtable table;
	private LinkedList list;

	public Cache(int maximumSize) {
		this.size = 0;
		this.maximumSize = maximumSize;
		this.table = new Hashtable(maximumSize);
		this.list = new LinkedList();
	}

	public synchronized void put(Object key, Object val) {
		if (size < maximumSize) {
			table.put(key, val);
			list.addFirst(key);
			size++;
		} else {
			if (!list.remove(key)) {
				if (list.size() > 0) {
					table.remove(list.removeLast());
				}
			}
			list.addFirst(key);
			table.put(key, val);
		}
	}

	public synchronized Object get(Object key) {
		return table.get(key);
	}

	public synchronized void clear() {
		table.clear();
		list.clear();
		size = 0;
	}

	public int getSize() {
		return size;
	}

	public int getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(int max) {
		maximumSize = max;
	}

}
