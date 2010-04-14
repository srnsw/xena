
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

final class VMOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"save", "restore", "setglobal", "currentglobal", "gcheck",
		"vmstatus", "defineuserobject", "undefineuserobject",
		"execuserobject", "vmreclaim", "setvmthreshold"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, VMOp.class);
	}

	static void save(Interpreter ip) {
		ip.ostack.pushRef(ip.vm.save(ip));
	}

	static void restore(Interpreter ip) {
		ip.vm.restore(ip, (SaveType) ip.ostack.pop(SAVE));
	}

	static void setglobal(Interpreter ip) {
		ip.vm.setGlobal(ip.ostack.popBoolean());
	}

	static void currentglobal(Interpreter ip) {
		ip.ostack.pushRef(new BoolType(ip.vm.isGlobal()));
	}

	static void gcheck(Interpreter ip) {
		ip.ostack.pushRef(new BoolType(ip.ostack.pop().isGlobal()));
	}

	static void vmstatus(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.vm.getSaveLevel()));
		ip.ostack.pushRef(new IntegerType(ip.vm.getUsage()));
		ip.ostack.pushRef(new IntegerType(ip.vm.getMaxUsage() * 4096));
	}

	static void vmreclaim(Interpreter ip) {
		int code = ip.ostack.popInteger();
		// TODO: implement vmreclaim
	}

	static void setvmthreshold(Interpreter ip) {
		int code = ip.ostack.popInteger();
		// TODO: implement setvmthreshold
	}

	static void defineuserobject(Interpreter ip) {
		Any any = ip.ostack.pop();
		int index = ip.ostack.popInteger();
		DictType userdict = (DictType) ip.systemdict.get("userdict");
		Any userobjects = (ArrayType) userdict.get("UserObjects");
		boolean global = ip.vm.isGlobal();
		ip.vm.setGlobal(false);
		ArrayType array;
		if (userobjects == null || !(userobjects instanceof ArrayType)) {
			array = new ArrayType(ip.vm, Math.max(10, index+10));
			userdict.put(ip.vm, "UserObjects", array);
		} else {
			ArrayType oldArray = (ArrayType) userobjects;
			if (oldArray.length() <= index) {
				array = new ArrayType(ip.vm, index+10);
				array.putinterval(ip.vm, 0, oldArray);
				userdict.put(ip.vm, "UserObjects", array);
			} else {
				array = oldArray;
			}
		}
		array.put(ip.vm, index, any);
		ip.vm.setGlobal(global);
	}

	static void undefineuserobject(Interpreter ip) {
		int index = ip.ostack.popInteger();
		DictType userdict = (DictType) ip.systemdict.get("userdict");
		Any userobjects = userdict.get("UserObjects");
		if (userobjects != null && userobjects instanceof ArrayType) {
			ArrayType array = (ArrayType) userobjects;
			array.put(ip.vm, index, new NullType());
		}
	}

	static void execuserobject(Interpreter ip) {
		int index = ip.ostack.popInteger();
		DictType userdict = (DictType) ip.systemdict.get("userdict");
		Any userobjects = userdict.get("UserObjects");
		if (userobjects == null)
			throw new Stop(UNDEFINED);
		if (!(userobjects instanceof ArrayType))
			throw new Stop(TYPECHECK, "UserObjects");
		ArrayType array = (ArrayType) userobjects;
		ip.estack.push(array.get(index));
	}

}
