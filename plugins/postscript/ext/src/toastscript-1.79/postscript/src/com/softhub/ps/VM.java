
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

import java.util.Vector;

public class VM {

	private static final int INITIAL_SIZE = 1024;
	private static final int INITIAL_SAVE_LEVEL = 1;

	private Vector localMemory = new Vector(INITIAL_SIZE);
	private Vector globalMemory = new Vector(INITIAL_SIZE);
	private int currentSaveLevel = INITIAL_SAVE_LEVEL;
	private boolean	global;
	private boolean stringbug;

	public int getSaveLevel() {
		return currentSaveLevel;
	}

	public boolean isInitialSaveLevel() {
		return currentSaveLevel <= INITIAL_SAVE_LEVEL;
	}

	public int getUsage() {
		return localMemory.size();
	}

	public int getMaxUsage() {
		return localMemory.capacity();
	}

	public SaveType save(Interpreter ip) {
		SaveType save = new SaveType(ip, currentSaveLevel++, localMemory.size());
		ip.gsave();
		return save;
	}

	public void restore(Interpreter ip, SaveType save) {
		int level = save.getLevel();
		ip.ostack.check(level);
		ip.estack.check(level);
		ip.dstack.check(level);
		int index = save.getVMIndex();
		restoreLevel(level, index, localMemory);
		currentSaveLevel = level;
		ip.grestoreAll();
		ip.arraypacking = save.getPackingMode();
		setGlobal(save.isAllocationModeGlobal());
	}

	private void restoreLevel(int level, int index, Vector memory) {
		for (int i = memory.size()-1; i >= index; i--) {
			CompositeType.Node node = (CompositeType.Node) memory.elementAt(i);
			node.restoreLevel(this, level);
		}
		memory.setSize(index);
	}

	public void add(CompositeType.Node node) {
		Vector memory = global ? globalMemory : localMemory;
		memory.addElement(node);
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setStringBug(boolean state) {
		stringbug = state;
	}

	public boolean getStringBug() {
		return stringbug;
	}

}
