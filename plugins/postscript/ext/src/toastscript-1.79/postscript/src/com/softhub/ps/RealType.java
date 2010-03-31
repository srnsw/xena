
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

public class RealType extends NumberType {

	private double val;

	public RealType(double val) {
		this.val = val;
	}

	public boolean isReal() {
		return true;
	}

	public int intValue() {
		return (int) val;
	}

	public float floatValue() {
		return (float) val;
	}

	public double realValue() {
		return val;
	}

	public Object cvj() {
		return new Double(floatValue());
	}

	public int typeCode() {
		return REAL;
	}

	public String typeName() {
		return "realtype";
	}

	public boolean equals(Object obj) {
		return obj instanceof RealType && ((RealType) obj).val == val;
	}

	public String toString() {
		return String.valueOf(val);
	}

	public int hashCode() {
		long bits = Double.doubleToLongBits(val);
		return (int)(bits ^ (bits >> 32));
	}

}
