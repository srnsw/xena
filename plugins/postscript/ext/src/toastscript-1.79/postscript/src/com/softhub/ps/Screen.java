
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

public class Screen implements Cloneable {

	private double freq;
	private double angle;
	private ArrayType proc;

	public Screen(double freq, double angle, ArrayType proc) {
		this.freq = freq;
		this.angle = angle;
		this.proc = proc;
	}

	public Object clone() throws CloneNotSupportedException {
		Screen screen = (Screen) super.clone();
		screen.proc = (ArrayType) proc.clone();
		return screen;
	}

	public double getFrequency() {
		return freq;
	}

	public double getAngle() {
		return angle;
	}

	public ArrayType getProcedure(VM vm) {
		if (proc == null) {
			proc = new ArrayType(vm, 0);
			proc.cvx();
		}
		return proc;
	}

}
