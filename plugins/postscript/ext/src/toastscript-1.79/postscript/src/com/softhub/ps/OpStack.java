
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

public class OpStack extends Stack {

	private final static int INITIAL_SIZE = 500;

	public OpStack() {
		super(INITIAL_SIZE);
	}

	public Any pop() {
		if (size > INITIAL_SIZE && count < INITIAL_SIZE / 2) {
			resize(INITIAL_SIZE);
		}
		return super.pop();
	}

	public Any pop(int type) {
		if (size > INITIAL_SIZE && count < INITIAL_SIZE / 2) {
			resize(INITIAL_SIZE);
		}
		return super.pop(type);
	}

	public void remove(int n) {
		super.remove(n);
		if (size > INITIAL_SIZE && count < INITIAL_SIZE / 2) {
			resize(INITIAL_SIZE);
		}
	}

	protected void resize(int newSize) {
		if (size == newSize)
			return;
		Any newArray[] = new Any[newSize];
		System.arraycopy(array, 0, newArray, 0, count);
		size = newSize;
		array = newArray;
	}

}
