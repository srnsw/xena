
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

import java.util.Enumeration;
import java.awt.geom.AffineTransform;

public class ArrayType extends CompositeType implements Enumerable, Interval {

	/**
	 * The shared value.
	 */
	private ArrayNode node;

	/**
	 * The start index of the array.
	 */
	private int index;

	/**
	 * The number of elements in the array.
	 */
	private int count;

	public ArrayType(VM vm, int size) {
		super(vm.isGlobal());
		node = new ArrayNode(vm, size);
		count = size;
	}

	public ArrayType(VM vm, int n, Stack stack) {
		this(vm, n);
		int offset = stack.count() - n;
		for (int i = 0; i < n; i++) {
			put(vm, i, (Any) stack.elementAt(i+offset));
		}
	}

	private ArrayType(ArrayType array, int index, int count) {
		super(array);
		this.node = array.node;
		this.index = index;
		this.count = count;
	}

	public int typeCode() {
		return ARRAY;
	}

	public String typeName() {
		return isPacked() ? "packedarraytype" : "arraytype";
	}

	public Object cvj() {
		if (length() == 0)
			throw new Stop(TYPECHECK, "zero length");
		Object obj = get(0).cvj();
		if (obj instanceof Boolean)
			return packBoolean();
		if (obj instanceof Number)
			return packNumber();
		if (obj instanceof String)
			return packString();
		throw new Stop(TYPECHECK, obj + " not packable");
	}

	private boolean[] packBoolean() {
		int i, n = length();
		boolean result[] = new boolean[n];
		for (i = 0; i < n; i++) {
			Object obj = get(i).cvj();
			if (!(obj instanceof Boolean))
				return null;
			result[i] = ((Boolean) obj).booleanValue();
		}
		return result;
	}

	private Object packNumber() {
		int i, n = length();
		Number array[] = new Number[n];
		for (i = 0; i < n; i++) {
			Object obj = get(i).cvj();
			if (!(obj instanceof Number))
				return null;
			array[i] = (Number) obj;
		}
		return array;
	}

	private String[] packString() {
		int i, n = length();
		String result[] = new String[n];
		for (i = 0; i < n; i++) {
			Object obj = get(i).cvj();
			if (!(obj instanceof String))
				return null;
			result[i] = obj.toString();
		}
		return result;
	}

	public void put(VM vm, int m, Any any) {
		if (!vm.isInitialSaveLevel() && isGlobal() && !any.isGlobal())
			throw new Stop(INVALIDACCESS, "put(array,global)");
		if (!wcheck())
			throw new Stop(INVALIDACCESS, "put(array,wcheck)");
		int i = m + index;
		if (i < 0 || m >= count)
			throw new Stop(RANGECHECK);
		node.put(vm, i, any);
	}

	public Any get(int m) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS, "put(array,rcheck)");
		int i = m + index;
		if (i < 0 || m >= count)
			throw new Stop(RANGECHECK);
		return node.get(i);
	}

	public void putinterval(VM vm, int m, Any any) {
		if (!(wcheck() && any.rcheck()))
			throw new Stop(INVALIDACCESS, "putinterval(array,wcheck)");
		if (!(any instanceof ArrayType))
			throw new Stop(TYPECHECK, "bad param: " + any);
		ArrayType array = (ArrayType) any;
		int i = m + index;
		if (i < 0 || m + array.count > count)
			throw new Stop(RANGECHECK);
		node.putinterval(vm, array.index, array.node, i, array.count, isGlobal());
	}

	public Any getinterval(int m, int n) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS, "getinterval(array,rcheck)");
		int i = m + index;
		if (i < 0 || m + n > count)
			throw new Stop(RANGECHECK);
		return new ArrayType(this, i, n);
	}

	public void put(VM vm, Any key, Any val) {
		if (!(key instanceof IntegerType))
			throw new Stop(TYPECHECK);
		put(vm, ((IntegerType) key).intValue(), val);
	}

	public Any get(Any key) {
		if (!(key instanceof IntegerType))
			throw new Stop(TYPECHECK);
		return get(((IntegerType) key).intValue());
	}

	public ArrayType put(VM vm, AffineTransform xform) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS, "put(array,xform)");
		if (length() != 6)
			throw new Stop(RANGECHECK);
		if (!check(NUMBER | NULL))
			throw new Stop(TYPECHECK);
		double m[] = new double[6];
		xform.getMatrix(m);
		node.put(vm, 0 + index, new RealType(m[0]));
		node.put(vm, 1 + index, new RealType(m[1]));
		node.put(vm, 2 + index, new RealType(m[2]));
		node.put(vm, 3 + index, new RealType(m[3]));
		node.put(vm, 4 + index, new RealType(m[4]));
		node.put(vm, 5 + index, new RealType(m[5]));
		return this;
	}

	void bind(Interpreter ip) {
		if (!isBound()) {
			int i, n = length();
			for (i = 0; i < n; i++) {
				Any any = get(i);
				if (any instanceof ArrayType) {
					ArrayType array = (ArrayType) any;
					array.bind(ip);
				} else if (any.isExecutable()) {
					Any val = ip.dstack.load(any);
					if (val != null && val instanceof OperatorType) {
						node.put(ip.vm, i, val);
					}
				}
			}
			setBound();
		}
	}

	public AffineTransform toTransform() {
		if (length() != 6)
			throw new Stop(RANGECHECK);
		if (!check(NUMBER))
			throw new Stop(TYPECHECK);
		return new AffineTransform(
			((NumberType) node.get(0)).floatValue(),
			((NumberType) node.get(1)).floatValue(),
			((NumberType) node.get(2)).floatValue(),
			((NumberType) node.get(3)).floatValue(),
			((NumberType) node.get(4)).floatValue(),
			((NumberType) node.get(5)).floatValue()
		);
	}

	public float[] toFloatArray() {
		int i, n = length();
		float array[] = new float[n];
		for (i = 0; i < n; i++) {
			array[i] = ((NumberType) node.get(i)).floatValue();
		}
		return array;
	}

	public boolean isMatrix() {
		return check(NUMBER, 6);
	}

	public boolean check(int typemask, int len) {
		if (length() != len)
			return false;
		for (int i = 0; i < len; i++) {
			Any any = node.get(i);
			if (!any.typeOf(typemask))
				return false;
		}
		return true;
	}

	public boolean check(int typemask) {
		return check(typemask, length());
	}

	public Enumeration keys() {
		throw new Stop(INTERNALERROR, "ArrayType.keys");
	}

	public Enumeration elements() {
		return new EnumElements();
	}

	public void exec(Interpreter ip) {
		if (isLiteral()) {
			ip.ostack.pushRef(this);
		} else {
			if (count > 0) {
				Any any = node.get(index++);
				if (--count > 0) {
					ip.estack.pushRef(this);
				}
				if (any.isLiteral() || any.typeOf(ARRAY)) {
					ip.ostack.push(any);
				} else {
					ip.estack.pushRef(any);
				}
			}
		}
	}

	public int length() {
		return count;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ArrayType) {
			ArrayType a = (ArrayType) obj;
			return node == a.node && index == a.index && count == a.count;
		}
		return false;
	}

	public String toString() {
		boolean literal = isLiteral();
		String val = literal ? "[ " : "{ ";
		val += node.toString(index, count);
		val += literal ? "]" : "}";
		return val;
	}

	public int hashCode() {
		return count + 43;
	}

	public int getSaveLevel() {
		return node.getSaveLevel();
	}

	class EnumElements implements Enumeration, Stoppable {

	    private int count;

	    public boolean hasMoreElements() {
			return count < length();
	    }

		public Object nextElement() {
			if (count >= length())
				throw new Stop(RANGECHECK);
			return get(count++);
		}

	}

	static class ArrayNode extends Node {

		private Any val[];

		ArrayNode(VM vm, int size) {
			super(vm);
			val = new Any[size];
		}

		ArrayNode(VM vm, ArrayNode node) {
			super(vm, node);
			val = new Any[node.val.length];
			System.arraycopy(node.val, 0, val, 0, node.val.length);
		}

		void copy(Node node) {
			super.copy(node);
			((ArrayNode) node).val = val;
		}

		void put(VM vm, int index, Any any) {
			if (checkLevel(vm))
				new ArrayNode(vm, this);
			val[index] = any;
		}

		void putinterval(VM vm, int srcIndex, ArrayNode src,
					     int dstIndex, int count, boolean dstIsGlobal)
		{
			if (checkLevel(vm))
				new ArrayNode(vm, this);
		    if (dstIsGlobal) {
				for (int i = 0; i < count; i++) {
					Any any = src.val[srcIndex++];
					if (!any.isGlobal())
						throw new Stop(INVALIDACCESS);
					val[dstIndex++] = any;
				}
		    } else {
				System.arraycopy(src.val, srcIndex, val, dstIndex, count);
		    }
		}

		Any get(int index) {
			Any value = val[index];
			return value == null ? new NullType() : value;
		}

		String toString(int index, int count) {
			String s = "";
			int i, n = index + count;
			for (i = index; i < n; i++)
				s += (val[i] + " ");
			return s;
		}

		public String toString() {
			return toString(0, val.length);
		}

	}

}
