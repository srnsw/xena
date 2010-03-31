
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

final class StackOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"clear", "count", "countexecstack", "countdictstack",
		"counttomark", "cleartomark", "index", "roll"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, StackOp.class);
		ip.installOp(new PopOp());
		ip.installOp(new DupOp());
		ip.installOp(new ExchOp());
	}

	static void clear(Interpreter ip) {
		ip.ostack.clear();
	}

	static void count(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.ostack.count()));
	}

	static void countexecstack(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.estack.count()));
	}

	static void countdictstack(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.dstack.count()));
	}

	static void counttomark(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.ostack.counttomark()));
	}

	static void cleartomark(Interpreter ip) {
		ip.ostack.cleartomark();
	}

	static void index(Interpreter ip) {
		ip.ostack.push(ip.ostack.index(ip.ostack.popInteger()));
	}

	static void roll(Interpreter ip) {
		int j = ip.ostack.popInteger();
		int n = ip.ostack.popInteger();
		ip.ostack.roll(n, j);
	}

	static class PopOp extends OperatorType {

		PopOp() {
			super("pop");
		}

		public void exec(Interpreter ip) {
			ip.ostack.pop();
		}

	}

	static class DupOp extends OperatorType {

		DupOp() {
			super("dup");
		}

		public void exec(Interpreter ip) {
			ip.ostack.dup();
		}

	}

	static class ExchOp extends OperatorType {

		ExchOp() {
			super("exch");
		}

		public void exec(Interpreter ip) {
			ip.ostack.exch();
		}

	}

}
