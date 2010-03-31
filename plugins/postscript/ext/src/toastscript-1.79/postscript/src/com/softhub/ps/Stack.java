
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

import java.io.PrintStream;

public class Stack implements Stoppable, Types {

	protected int size;
	protected int count;
	protected Any array[];
	private int bottom;

	/**
	 * Construct the stack.
	 * @param size the initial size
	 */
	public Stack(int size) {
		init(size);
	}

	/**
	 * Initialize the stack.
	 * @param size the initial size
	 */
	protected void init(int size) {
		this.size = size;
		this.count = 0;
		this.array = new Any[size];
	}

	/**
	 * Resize the stack.
	 * @param newSize the new size of stack
	 */
	protected void resize(int newSize) {
		throw new Stop(overflow(), toString());
	}

	/**
	 * Set the line number for the topmost element.
	 * @param any the parent object
	 */
	public void setLineNo(Any any) {
		array[count-1].setLineNo(any);
	}

	/**
	 * Duplicate topmost element.
	 * @return the copy of the topmost element
	 */
	public Any dup() {
		if (count <= 0)
			throw new Stop(underflow(), toString());
		if (count >= size) {
			resize(size * 2);
		}
		try {
			Any any = (Any) array[count-1].clone();
			array[count++] = any;
			return any;
		} catch (CloneNotSupportedException ex) {
			throw new Stop(INTERNALERROR);
		}
	}

	/**
	 * Push a copy of the parameter onto stack.
	 * @param any the element to push
	 */
	public void push(Any any) {
		if (count >= size) {
			resize(size * 2);
		}
		try {
			array[count++] = (Any) any.clone();
		} catch (CloneNotSupportedException ex) {
			throw new Stop(INTERNALERROR, "Stack.push");
		}
	}

	/**
	 * Push an element by reference. Usually this should
	 * only be used, if the element is moved from one stack
	 * to another, or if the object is newly created.
	 * @param any the element to push
	 */
	public void pushRef(Any any) {
		if (count >= size) {
			resize(size * 2);
		}
		array[count++] = any;
	}

	/**
	 * Set the bottom marker.
	 */
	public void setBottom() {
		bottom = count;
	}

	/**
	 * Clear the stack.
	 */
	public void clear() {
		remove(count - bottom);
	}

	/**
	 * Pop the stack and return the topmost value.
	 * @return a value of any type
	 */
	public Any pop() {
		if (count <= bottom)
			throw new Stop(underflow(), toString());
		return array[--count];
	}

	/**
	 * Pop the stack and return the topmost value.
	 * @param the expected data type mask
	 * @return a value of any type
	 */
	public Any pop(int type) {
		if (count <= bottom)
			throw new Stop(underflow(), toString());
		Any any = array[--count];
		if ((any.typeCode() & type) == 0)
			throw new Stop(TYPECHECK, "pop: " + any.typeName());
		return any;
	}

	/**
	 * @return a value of type IntegerType
	 */
	public int popInteger() {
		return ((IntegerType) pop(INTEGER)).intValue();
	}


	/**
	 * @return a value of type NumberType
	 */
	public double popNumber() {
		return ((NumberType) pop(NUMBER)).realValue();
	}

	/**
	 * @return a value of type BoolType
	 */
	public boolean popBoolean() {
		return ((BoolType) pop(BOOLEAN)).booleanValue();
	}

	/**
	 * @return a value of type String
	 */
	public String popString() {
		return pop(STRING | NAME).toString();
	}

	/**
	 * Get a copy of the top element of stack, leaving
	 * the stack unchanged.
	 * @return the element at top of stack
	 */
	public Any top() {
		if (count <= 0)
			throw new Stop(underflow(), toString());
		return array[count-1];
	}

	/**
	 * Get a copy of the top element of stack, leaving
	 * the stack unchanged.
	 * @param type the expected type
	 * @return the element at top of stack
	 */
	public Any top(int type) {
		if (count <= 0)
			throw new Stop(underflow(), toString());
		Any any = array[count-1];
		if ((any.typeCode() & type) == 0)
			throw new Stop(TYPECHECK, "top: " + any.typeName());
		return any;
	}

	/**
	 * Access the elements relative to top of stack.
	 * @param the index
	 * @return the element at index
	 */
	public Any index(int index) {
		if (index < 0 || index >= count)
			throw new Stop(RANGECHECK);
		return array[count-index-1];
	}

	/**
	 * @param the index
	 * @param type the expected type
	 * @return the element at index
	 */
	public Any index(int index, int type) {
		if (index < 0 || index >= count)
			throw new Stop(RANGECHECK);
		Any any = array[count-index-1];
		if ((any.typeCode() & type) == 0)
			throw new Stop(TYPECHECK, "index: " + any.typeName());
		return any;
	}

	/**
	 * Access the elements like an array, indexed begining
	 * at bottom of stack.
	 * @param the index
	 * @return the element at index
	 */
	public Any elementAt(int index) {
		if (index < 0 || index >= count)
			throw new Stop(RANGECHECK, toString());
		return array[index];
	}

	/**
	 * Exchange the two topmost elements.
	 */
	public void exch() {
		if (count < 2)
			throw new Stop(underflow(), toString());
		int index1 = count-1;
		int index2 = count-2;
		Any tmp = array[index1];
		array[index1] = array[index2];
		array[index2] = tmp;
	}

	/**
	 * Roll the stack.
	 * @param n the number of elements to roll
	 * @param j the number of rolls
	 */
	public void roll(int n, int j) {
		if (count < n)
			throw new Stop(underflow(), toString());
		if (n < 0)
			throw new Stop(RANGECHECK, toString());
		if (j != 0 && n != 0) {
			// TODO: this could be improved!
			int offset = count - n;
			if (j > 0) {
				while (j-- > 0) {
					Any tmp = array[count-1];
					for (int i = offset+n-1; i > offset; i--)
						array[i] = array[i-1];
					array[offset] = tmp;
				}
			} else {
				while (j++ < 0) {
					Any tmp = array[offset];
					for (int i = offset; i < count-1; i++)
						array[i] = array[i+1];
					array[count-1] = tmp;
				}
			}
		}
	}

	/**
	 * @return the number of elements on the stack
	 */
	public int count() {
		return count;
	}

	/**
	 * @param typecode the type we are looking for
	 * @return the number of elements to topmost type
	 */
	public int countto(int typecode) {
		int n = count-1;
		int m = n;
		while (m >= 0 && (array[m].typeCode() & typecode) == 0)
			m--;
		return n - m;
	}

	/**
	 * @return the number of elements to topmost mark
	 */
	public int counttomark() {
		int m = countto(MARK);
		if (m >= count)
			throw new Stop(UNMATCHEDMARK, toString());
		return m;
	}

	/**
	 * Pop the stack until we find a mark.
	 */
	public void cleartomark() {
		int m = countto(MARK);
		if (m >= count)
			throw new Stop(UNMATCHEDMARK);
		remove(m+1);
	}

	/**
	 * Remove n elements from the stack.
	 * @param n the number of elements to remove from stack
	 */
	public void remove(int n) {
		for (int i = 0; i < n; i++) {
			array[--count] = null;
		}
	}

	/**
	 * Check stack for invalid objects.
	 * @param the current save level
	 */
	public void check(int level) {
		for (int i = count-1; i >= 0; i--) {
			if (array[i] instanceof CompositeType) {
				CompositeType comp = (CompositeType) array[i];
				if (comp.getSaveLevel() > level) {
					throw new Stop(INVALIDRESTORE, comp.toString());
				}
			}
		}
	}

	/**
	 * @return the error code for stack overflows
	 */
	protected int overflow() {
		return STACKOVERFLOW;
	}

	/**
	 * @return the error code for stack underflows
	 */
	protected int underflow() {
		return STACKUNDERFLOW;
	}

	/**
	 * Print string representation of object on the stack.
	 * @param out the output stream
	 */
	public void print(PrintStream out) {
		for (int i = 0; i < count; i++) {
			String val = escapeSpecialChars(array[i].toString());
			String type = array[i].typeName();
			out.println("" + (count-i-1) + ": " + val + " <" + type + ">");
		}
	}

	/**
	 * @return string representation using escape sequences
	 */
	private static String escapeSpecialChars(String s) {
		StringBuffer buf = new StringBuffer();
		int i, n = s.length();
		for (i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\t':
				buf.append("\\t");
				break;
			case '\r':
				buf.append("\\r");
				break;
			case '\n':
				buf.append("\\n");
				break;
			case '\f':
				buf.append("\\f");
				break;
			case '\b':
				buf.append("\\b");
				break;
			case '\0':
				buf.append("\\0");
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}

	/**
	 * @return string representation
	 */
	public String toString() {
		return "stack<" + size + ">";
	}

}
