
package com.softhub.ps;

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
import java.util.Enumeration;

public class DictType extends CompositeType implements Enumerable {

	private int initialSize;
	private DictNode node;

	public DictType(VM vm, int size) {
		this(vm, size, false);
	}

	public DictType(VM vm, int size, boolean global) {
		super(global);
		initialSize = size;
		node = new DictNode(vm, size);
	}

	public DictType(VM vm, int n, Stack stack) {
		this(vm, n / 2);
		int offset = stack.count() - n;
		for (int i = offset; i < n+offset; i += 2) {
			put(vm, (Any) stack.elementAt(i), (Any) stack.elementAt(i+1));
		}
	}

	public DictType(VM vm, DictType dict) {
		super(dict);
		initialSize = dict.initialSize;
		node = new DictNode(vm, dict.node);
	}

	public DictType copyTo(VM vm, DictType dict) {
		Enumeration keyIter = keys();
		Enumeration valIter = elements();
		while (keyIter.hasMoreElements()) {
			Any key = (Any) keyIter.nextElement();
			Any val = (Any) valIter.nextElement();
			dict.put(vm, key, val);
		}
		return dict;
	}

	public int typeCode() {
		return DICT;
	}

	public String typeName() {
		return "dicttype";
	}

	public void put(VM vm, Any key, Any val) {
		if (!vm.isInitialSaveLevel() && isGlobal() && (!key.isGlobal() || !val.isGlobal()))
			throw new Stop(INVALIDACCESS, "put(dict,global)" + key.isGlobal() + " " + val.isGlobal());
		node.put(vm, key, val);
	}

	public void put(VM vm, String key, Any val) {
		put(vm, new NameType(key), val);
	}

	public Any get(Any key) {
		return node.get(key);
	}

	public Any get(Any key, int mask) {
		return node.get(key, mask);
	}

	public Any get(String key) {
		return node.get(new NameType(key));
	}

	public Any get(String key, int mask) {
		return node.get(new NameType(key), mask);
	}

	public void remove(VM vm, Any key) {
		node.remove(vm, key);
	}

	public boolean known(Any key) {
		if (key instanceof NullType)
			throw new Stop(TYPECHECK);
		return node.get(key) != null;
	}

	public boolean known(String key) {
		return node.get(new NameType(key)) != null;
	}

	public int length() {
		return node.length();
	}

	public int maxlength() {
		int len = length();
		return len < initialSize ? initialSize : len;
	}

	public Enumeration elements() {
		return node.elements();
	}

	public Enumeration keys() {
		return node.keys();
	}

	public void exec(Interpreter ip) {
		ip.ostack.push(this);
	}

	public boolean equals(Object obj) {
		return obj instanceof DictType && ((DictType) obj).node == node;
	}

	/**
	 * Temporarily overwrite the access attributes.
	 * @return the access attributes
	 */
	public int saveAccessFlags() {
		return node.saveAccessFlags();
	}

	/**
	 * Restore the access attributes.
	 * @param flags the access attributes
	 */
	public void restoreAccessFlags(int flags) {
		node.restoreAccessFlags(flags);
	}

	public boolean rcheck() {
		return node.rcheck();
	}

	public boolean wcheck() {
		return node.wcheck();
	}

	public Any executeonly() {
		node.executeonly();
		return this;
	}

	public Any noaccess() {
		node.noaccess();
		return this;
	}

	public Any readonly() {
		node.readonly();
		return this;
	}

	public void setFontAttr() {
		node.setFontAttr();
	}
	
	public boolean getFontAttr() {
		return node.getFontAttr();
	}

	public String toString() {
		return "dict<" + length() + ">";
	}

	public int hashCode() {
		return initialSize + node.hashCode();
	}

	public int getSaveLevel() {
		return node.getSaveLevel();
	}

	static class DictNode extends Node implements Stoppable {

		private final static int MIN_DICT_SIZE = 8;

		private final static int RMODE_BIT = 1;
		private final static int WMODE_BIT = 2;
		private final static int XMODE_BIT = 4;
		private final static int FONT_BIT = 8;

		private int	flags = RMODE_BIT | WMODE_BIT | XMODE_BIT;

		private Hashtable map;

		DictNode(VM vm, int size) {
			super(vm);
			map = new Hashtable(Math.max(size, MIN_DICT_SIZE));
		}

		DictNode(VM vm, DictNode node) {
			super(vm, node);
			flags = node.flags;
			map = (Hashtable) node.map.clone();
		}

		void copy(Node node) {
			super.copy(node);
			((DictNode) node).map = map;
		}

		void put(VM vm, Any key, Any val) {
			if (checkLevel(vm))
				new DictNode(vm, this);
			if (!wcheck())
				throw new Stop(INVALIDACCESS, "put " + key + " "  + val + " " + this);
			if (key instanceof NullType)
				throw new Stop(TYPECHECK);
			if (key instanceof StringType) {
				map.put(new NameType((StringType) key), val);
			} else {
				map.put(key, val);
			}
		}

		Any get(Any key) {
			if (!rcheck())
				throw new Stop(INVALIDACCESS, "get " + key);
			if (key instanceof NullType)
				throw new Stop(TYPECHECK);
			if (key instanceof StringType)
				key = new NameType((StringType) key);
			return (Any) map.get(key);
		}

		Any get(Any key, int mask) {
			Any val = get(key);
			if (val == null || !val.typeOf(mask))
				throw new Stop(TYPECHECK, "get " + key);
			return val;
		}

		void remove(VM vm, Any key) {
			if (!wcheck())
				throw new Stop(INVALIDACCESS, "remove " + key);
			if (checkLevel(vm))
				new DictNode(vm, this);
			if (key instanceof NullType)
				throw new Stop(TYPECHECK);
			map.remove(key);
		}

		int length() {
			return map.size();
		}

		public Enumeration elements() {
			return map.elements();
		}

		public Enumeration keys() {
			return map.keys();
		}

		public int hashCode() {
			return map.hashCode();
		}

		/**
		 * Temporarily overwrite the access attributes.
		 * @return the access attributes
		 */
		public int saveAccessFlags() {
			int save = flags;
			flags |= RMODE_BIT | WMODE_BIT | XMODE_BIT;
			return save;
		}
	
		/**
		 * Restore the access attributes.
		 * @param flags the access attributes
		 */
		public void restoreAccessFlags(int flags) {
			this.flags = flags;
		}

		public boolean rcheck() {
			return (flags & RMODE_BIT) != 0;
		}

		public boolean wcheck() {
			return (flags & WMODE_BIT) != 0;
		}

		public void executeonly() {
			flags &= XMODE_BIT | FONT_BIT;
		}

		public void noaccess() {
			flags = 0;
		}

		public void readonly() {
			flags &= RMODE_BIT | XMODE_BIT | FONT_BIT;
		}

		public void setFontAttr() {
			flags |= FONT_BIT;
		}

		public boolean getFontAttr() {
			return (flags & FONT_BIT) != 0;
		}

		public String toString() {
			return "DictNode<" + map.size() + ", " + flags + ">";
		}

	}

}
