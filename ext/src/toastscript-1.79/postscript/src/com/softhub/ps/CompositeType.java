
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

public abstract class CompositeType extends Any {

	public CompositeType() {
		this(true);
	}

	public CompositeType(boolean global) {
		super();
		if (global) {
			setGlobal();
		}
	}

	public CompositeType(CompositeType comp) {
		this(comp, false);
	}

	public CompositeType(CompositeType comp, boolean global) {
		super(comp);
		if (global) {
			setGlobal();
		}
	}

	public abstract int getSaveLevel();

	static class Node {

		private int createlevel;
		private int currentlevel;
		private Node prev;

		protected Node(VM vm) {
			createlevel = currentlevel = vm.getSaveLevel();
		}

		Node(VM vm, Node node) {
			createlevel = node.createlevel;
			currentlevel = node.currentlevel;
			prev = node.prev;
			node.currentlevel = vm.getSaveLevel();
			node.prev = this;
			vm.add(node);
		}

		void copy(Node node) {
			node.currentlevel = currentlevel;
			node.prev = prev;
		}

		int getSaveLevel() {
			return createlevel;
		}

		boolean checkLevel(VM vm) {
			return vm != null && !vm.isGlobal() && vm.getSaveLevel() > currentlevel;
		}

		void restoreLevel(VM vm, int level) {
			Node node = this;
			while (node != null && node.currentlevel > level) {
				node = node.prev;
			}
			if (node != null && node != this) {
				node.copy(this);
			}
		}

	}

}
