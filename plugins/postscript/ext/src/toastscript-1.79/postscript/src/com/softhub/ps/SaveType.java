
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

public class SaveType extends Any {

	private int level;
	private int vmindex;
	private boolean allocmode;
	private boolean packing;

	public SaveType(Interpreter ip, int level, int index) {
		this.level = level;
		vmindex = index;
		allocmode = ip.vm.isGlobal();
		packing = ip.arraypacking;
	}

	public int getLevel() {
		return level;
	}

	public int getVMIndex() {
		return vmindex;
	}

	public boolean isAllocationModeGlobal() {
		return allocmode;
	}

	public boolean getPackingMode() {
		return packing;
	}

	public int typeCode() {
		return SAVE;
	}

	public String typeName() {
		return "savetype";
	}

	protected boolean isGlobal() {
		return true;
	}

	public boolean equals(Object obj) {
		return obj instanceof SaveType && ((SaveType) obj).level == level;
	}

	public String toString() {
		return "save<" + level + ">";
	}

	public int hashCode() {
		return 73 + level;
	}

}
