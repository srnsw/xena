
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

public class BoolType extends NumberType {

	public final static BoolType TRUE = new BoolType(true);
	public final static BoolType FALSE = new BoolType(false);

	private boolean	val;

	public BoolType(boolean val) {
		this.val = val;
	}

	public BoolType(int val) {
		this.val = val != 0;
	}

	public int typeCode() {
		return BOOLEAN;
	}

	public String typeName() {
		return "booleantype";
	}

	protected boolean isGlobal() {
		return true;
	}

	public Object cvj() {
		return new Boolean(val);
	}

	public boolean isReal() {
		return false;
	}

	public boolean booleanValue() {
		return val;
	}

	public int intValue() {
		return val ? 1 : 0;
	}

	public boolean equals(Object obj) {
		return obj instanceof BoolType && ((BoolType) obj).val == val;
	}

	public String toString() {
		return String.valueOf(val);
	}

	public int hashCode() {
		return val ? 1 : 0;
	}

}
