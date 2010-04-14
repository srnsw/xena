
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

public class NameType extends Any {

	private static final int INITIAL_SIZE = 1024;

	private static Hashtable nameSpace = new Hashtable(INITIAL_SIZE);
	private static int nameIndex;

	private Node val;

	public NameType(String s) {
		val = load(s);
	}

	public NameType(char s[], int len) {
		val = load(new String(s, 0, len));
	}

	public NameType(StringType s) {
		val = load(s.toString());
		if (!s.isLiteral()) {
			cvx();
		}
	}

	public Object clone() throws CloneNotSupportedException {
		reload(val);
		return super.clone();
	}

	protected void finalize() throws Throwable {
		unload(val);
		super.finalize();
	}

	public int typeCode() {
		return NAME;
	}

	public String typeName() {
		return "nametype";
	}

	protected boolean isGlobal() {
		return true;
	}

	public void exec(Interpreter ip) {
		if (isLiteral()) {
			ip.ostack.pushRef(this);
		} else {
			Any any = ip.dstack.load(this);
			if (any == null)
				throw new Stop(UNDEFINED, toString());
			ip.estack.push(any);
			ip.estack.setLineNo(this);
		}
	}

	public int length() {
		return val.getName().length();
	}

	public char[] toCharArray() {
		return val.getName().toCharArray();
	}

	public String toString() {
		return val.getName();
	}

	public int hashCode() {
		return val.getID();
	}

	public boolean equals(Object obj) {
		if (obj instanceof NameType)
			return val == ((NameType) obj).val;
		if (obj instanceof StringType)
			return obj.equals(this);
		if (obj instanceof String)
			return obj.equals(val.getName());
		return false;
	}

	static void printStatistics() {
		System.out.println("Name Space: " + nameSpace.size());
	}

	private synchronized static Node load(String s) {
		Node node = (Node) nameSpace.get(s);
		if (node != null) {
			node.incRef();
			return node;
		}
		node = new Node(s, nameIndex++);
		nameSpace.put(s, node);
		return node;
	}

	private synchronized static void reload(Node node) {
		node.incRef();
	}

	private synchronized static void unload(Node node) {
		if (node.decRef() < 1) {
			nameSpace.remove(node.getName());
		}
	}

	static class Node {

		private String name;
		private int id;
		private int refCount = 1;

		Node(String name, int id) {
			this.name = name;
			this.id = id;
		}

		String getName() {
			return name;
		}

		int getID() {
			return id;
		}

		void incRef() {
			refCount++;
		}

		int decRef() {
			return --refCount;
		}

	}

}
