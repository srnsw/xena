
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

public class DictStack extends Stack {

	public DictStack(int size) {
		super(size);
	}

	public Any load(Any key) {
		Any val = null;
		for (int i = count-1; val == null && i >= 0; i--) {
			val = ((DictType) array[i]).get(key);
		}
		return val;
	}

	public Any load(String s) {
		return load(new NameType(s));
	}

	public DictType where(Any any) {
		for (int i = count-1; i >= 0; i--) {
			DictType dict = (DictType) array[i];
			if (dict.known(any))
				return dict;
		}
		return null;
	}

	public void def(VM vm, Any key, Any val) {
		((DictType) array[count-1]).put(vm, key, val);
	}

	public void store(VM vm, Any key, Any val) {
		DictType dict = where(key);
		if (dict == null) {
			((DictType) array[count-1]).put(vm, key, val);
		} else {
			dict.put(vm, key, val);
		}
	}

	public DictType currentdict() {
		return (DictType) array[count-1];
	}

	protected int overflow() {
		return DICTSTACKOVERFLOW;
	}

	protected int underflow() {
		return DICTSTACKUNDERFLOW;
	}

}
