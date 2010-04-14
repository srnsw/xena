
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

public abstract class NumberType extends Any {

	public int typeCode() {
		return NUMBER;
	}

	public void exec(Interpreter ip) {
		ip.ostack.pushRef(this);
	}

	protected boolean isGlobal() {
		return true;
	}

	public float floatValue() {
		return intValue();
	}

	public double realValue() {
		return intValue();
	}

	public abstract boolean isReal();
	public abstract int intValue();

}
