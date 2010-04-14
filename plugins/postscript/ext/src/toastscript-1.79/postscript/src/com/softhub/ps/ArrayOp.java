
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

import java.util.Enumeration;

final class ArrayOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"bind", "array", "packedarray", "setpacking",
		"currentpacking", "aload", "astore"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, ArrayOp.class);
		ip.installOp(new MarkOp("["));
		ip.installOp(new MarkOp("<<"));
		ip.installOp(new MarkOp("mark"));
		ip.installOp(new ArrayEndOp());
	}

	static void bind(Interpreter ip) {
		ArrayType array = (ArrayType) ip.ostack.top(ARRAY);
		array.bind(ip);
	}

	final static class MarkOp extends OperatorType {

		MarkOp(String name) {
			super(name);
		}

		public void exec(Interpreter ip) {
			ip.ostack.pushRef(new MarkType());
		}

	}

	final static class ArrayEndOp extends OperatorType {

		ArrayEndOp() {
			super("]");
		}

		public void exec(Interpreter ip) {
			int n = ip.ostack.counttomark();
			ArrayType array = new ArrayType(ip.vm, n, ip.ostack);
			ip.ostack.remove(n+1);
			ip.ostack.pushRef(array);
		}

	}

	static void array(Interpreter ip) {
		ip.ostack.pushRef(new ArrayType(ip.vm, ip.ostack.popInteger()));
	}

	static void packedarray(Interpreter ip) {
		int count = ip.ostack.popInteger();
		ArrayType array = new ArrayType(ip.vm, count, ip.ostack);
		array.setPacked(true);
		ip.ostack.remove(count);
		ip.ostack.pushRef(array);
	}

	static void setpacking(Interpreter ip) {
		ip.arraypacking = ip.ostack.popBoolean();
	}

	static void currentpacking(Interpreter ip) {
		ip.ostack.pushRef(new BoolType(ip.arraypacking));
	}

	static void aload(Interpreter ip) {
		ArrayType a = (ArrayType) ip.ostack.pop(ARRAY);
		Enumeration e = a.elements();
		while (e.hasMoreElements()) {
			ip.ostack.push((Any) e.nextElement());
		}
		ip.ostack.pushRef(a);
	}

	static void astore(Interpreter ip) {
		ArrayType a = (ArrayType) ip.ostack.pop(ARRAY);
		for (int i = a.length()-1; i >= 0; i--) {
			a.put(ip.vm, i, ip.ostack.pop());
		}
		ip.ostack.pushRef(a);
	}

}
