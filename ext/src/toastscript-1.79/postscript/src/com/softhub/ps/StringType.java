
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

import java.io.IOException;
import java.util.Enumeration;

public class StringType extends CharSequenceType implements Enumerable, Interval {

	/**
	 * The shared data.
	 */
	private StringNode node;

	/**
	 * The start index of the (sub-) string.
	 */
	private int index;

	/**
	 * The length of the (sub-) string.
	 */
	private int count;

	/**
	 * The scanner. Non-null if executed or tokenized.
	 */
	private Scanner scanner;

	public StringType(VM vm, int len) {
		super(vm);
		this.node = new StringNode(vm, len);
		this.count = len;
	}

	public StringType(VM vm, char val[]) {
		this(vm, val, val.length);
	}

	public StringType(VM vm, char val[], int len) {
		this(vm, len);
		this.node = new StringNode(vm, val, 0, len);
		this.count = len;
	}

	public StringType(VM vm, NameType val) {
		this(vm, val.toCharArray());
	}

	public StringType(VM vm, String val) {
		super(vm);
		this.count = val.length();
		char chars[] = new char[count];
		val.getChars(0, count, chars, 0);
		this.node = new StringNode(vm, chars, 0, count);
	}

	public StringType(VM vm, StringType string, char val[]) {
		super(vm, string);
		this.node = string.node;
		this.count = val.length;
		if (count > string.length())
			throw new Stop(RANGECHECK);
		node.putinterval(vm, 0, val, string.index, count);
	}

	public StringType(StringType string, int index, int count) {
		super(string);
		this.node = string.node;
		this.index = index;
		this.count = count;
	}

	public int typeCode() {
		return STRING;
	}

	public String typeName() {
		return "stringtype";
	}

	public Object cvj() {
		return toString();
	}

	public int getchar() throws IOException {
		if (index < 0)
			throw new IOException();
		if (count >= 0)
			count--;
		if (count < 0)
			return -1;
		return node.get(index++);
	}

	public void ungetchar(int ch) throws IOException {
		if (index <= 0)
			throw new IOException();
		if (count >= 0) {
			index--;
			count++;
		}
	}

	public void putchar(int c) throws IOException {
		throw new IOException();
	}

	public void close() {
		scanner = null;
	}

	public void put(VM vm, int m, int val) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS);
		if (val < 0 || val > 65535)
			throw new Stop(RANGECHECK);
		int i = m + index;
		if (i < 0 || m >= length())
			throw new Stop(RANGECHECK);
		node.put(vm, i, val);
	}

	public int get(int m) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		int i = m + index;
		if (i < 0 || m >= length())
			throw new Stop(RANGECHECK);
		return node.get(i);
	}

	public void putinterval(VM vm, int m, Any any) {
		if (!(wcheck() && any.rcheck()))
			throw new Stop(INVALIDACCESS);
		if (!(any instanceof StringType))
			throw new Stop(TYPECHECK, "bad param: " + any);
		StringType string = (StringType) any;
		int i = m + index;
		int slen = string.length();
		if (i < 0 || m + slen > length())
			throw new Stop(RANGECHECK);
		node.putinterval(vm, string.index, string.node, i, slen);
	}

	public Any getinterval(int offset, int n) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		int i = offset + index;
		if (i < 0 || i+n > index + count)
			throw new Stop(RANGECHECK,
				    "len: " + length() + " i: " + i + " n: " + n +
					" index: " + index + " count: " + count
			);
		return new StringType(this, i, n);
	}

	public void put(VM vm, Any key, Any val) {
		if (!(key instanceof IntegerType && val instanceof IntegerType))
			throw new Stop(TYPECHECK);
		put(vm, ((IntegerType) key).intValue(), ((IntegerType) val).intValue());
	}

	public Any get(Any key) {
		if (!(key instanceof IntegerType))
			throw new Stop(TYPECHECK);
		return new IntegerType(get(((IntegerType) key).intValue()));
	}

	public Enumeration keys() {
		throw new Stop(INTERNALERROR);
	}

	public Enumeration elements() {
		return new EnumElements();
	}

	public void exec(Interpreter ip) {
		if (isLiteral()) {
			ip.ostack.push(this);
		} else {
			ip.estack.pushRef(this);
			if (scanner == null)
				scanner = new Scanner();
			if (ip.scan(this, scanner, true) == Scanner.EOF) {
				ip.estack.pop();
				scanner = null;
			}
		}
	}

	/**
	 * Read a token from this string and push it onto the operand stack.
	 * @param ip the ps-interpreter
	 * @return true if there are more tokens
	 */
	public boolean token(Interpreter ip) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		if (scanner == null)
			scanner = new Scanner();
		int type = ip.scan(this, scanner, false);
		if (type != Scanner.EOF) {
			ip.ostack.pushRef(this);
			ip.ostack.exch();
		}
		return type != Scanner.EOF;
	}

	/**
	 * @return the string length
	 */
	public int length() {
		return count >= 0 ? count : 0;
	}

	/**
	 * Test if two strings are equal by comparing each character.
	 * @param obj the other string object
	 * @return true if equal; false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj instanceof StringType)
			return node.equals(((StringType) obj).toCharArray(), index, length());
		if (obj instanceof NameType)
			return node.equals(((NameType) obj).toCharArray(), index, length());
		if (obj instanceof String)
			return node.equals(((String) obj).toCharArray(), index, length());
		return false;
	}

	/**
	 * @return an array of characters of the (sub-) string
	 */
	public char[] toCharArray() {
		return node.toCharArray(index, length());
	}

	/**
	 * @return a string representation
	 */
	public String toString() {
		return node.toString(index, length());
	}

	/**
	 * @return a hash code for this string
	 *
	 * Note: Strings in PS are mutable, but if we
	 *       use strings as keys in dictionaries,
	 *       we convert them to names and this hash
	 *       will not be used.
	 */
	public int hashCode() {
		throw new Stop(INTERNALERROR, "attempt to use string in dict");
	}

	/**
	 * @return a the shared data's save level
	 */
	public int getSaveLevel() {
		return node.getSaveLevel();
	}

	class EnumElements implements Enumeration, Stoppable {

		private int current;
		private int length;

		EnumElements() {
			this.length = length();
		}

	    public boolean hasMoreElements() {
			return current < length;
	    }

		public Object nextElement() {
			if (current >= length)
				throw new Stop(RANGECHECK);
			return new IntegerType(get(current++));
		}

	}

	static class StringNode extends Node {

		private char val[];

		StringNode(VM vm, int size) {
			super(vm);
			val = new char[size];
		}

		StringNode(VM vm, char val[], int index, int count) {
			this(vm, count);
			System.arraycopy(val, index, this.val, 0, count);
		}

		StringNode(VM vm, StringNode node) {
			super(vm, node);
			val = new char[node.val.length];
			System.arraycopy(node.val, 0, val, 0, node.val.length);
		}

		void copy(Node node) {
			super.copy(node);
			((StringNode) node).val = val;
		}

		void put(VM vm, int index, int c) {
			if (!vm.getStringBug() && checkLevel(vm))
				new StringNode(vm, this);
			val[index] = (char) c;
		}

		void putinterval(VM vm, int srcIndex, StringNode node, int dstIndex, int count) {
			putinterval(vm, srcIndex, node.val, dstIndex, count);
		}

		void putinterval(VM vm, int srcIndex, char src[], int dstIndex, int count) {
			if (checkLevel(vm))
				new StringNode(vm, this);
			System.arraycopy(src, srcIndex, this.val, dstIndex, count);
		}

		int get(int index) {
			return val[index];
		}

		int length() {
			return val.length;
		}

		boolean equals(char array[], int index, int count) {
			if (array.length != count)
				return false;
			for (int i = 0; i < count; i++) {
				if (val[index+i] != array[i])
					return false;
			}
			return true;
		}

		char[] toCharArray(int index, int count) {
			char array[] = new char[count];
			System.arraycopy(val, index, array, 0, count);
			return array;
		}

		String toString(int index, int count) {
			int i, n = index + count;
			for (i = index; i < n; i++) {
				if (val[i] == '\0') {
					count = i - index;
					break;
				}
			}
			return new String(val, index, count);
		}

		public String toString() {
			return toString(0, val.length);
		}

	}

}
