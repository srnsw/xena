
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

final class TypeOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"type", "cvlit", "cvx", "xcheck", "cvn", "cvi",
		"cvr", "cvrs", "cvs", "executeonly", "noaccess",
		"readonly", "rcheck", "wcheck"
	};

	private final static String NOSTRINGVAL = "--nostringval--";

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, TypeOp.class);
	}

	static void type(Interpreter ip) {
		ip.ostack.pushRef(new NameType(ip.ostack.pop().typeName()));
	}

	static void cvlit(Interpreter ip) {
		ip.ostack.top().cvlit();
	}

	static void cvx(Interpreter ip) {
		ip.ostack.top().cvx();
	}

	static void xcheck(Interpreter ip) {
		Any any = ip.ostack.pop();
		ip.ostack.pushRef(new BoolType(any.isExecutable()));
	}

	static void cvn(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		ip.ostack.pushRef(new NameType(s));
	}

	static void cvi(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | STRING);
		int result = 0;
		if (any instanceof StringType) {
			double val = 0;
			try {
				val = Double.valueOf(new String(((StringType) any).toCharArray())).doubleValue();
			} catch (NumberFormatException ex) {
				// TODO: other NumberType formats
				throw new Stop(TYPECHECK, "cvi");
			}
			if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE)
				throw new Stop(RANGECHECK);
			result = (int) val;
		} else {
			result = ((NumberType) any).intValue();
		}
		ip.ostack.pushRef(new IntegerType(result));
	}

	static void cvr(Interpreter ip) {
		Any any = ip.ostack.pop(NUMBER | STRING);
		double result = 0;
		if (any instanceof StringType) {
			StringType s = (StringType) any;
			boolean moreTokens = s.token(ip);
			if (!moreTokens)
				throw new Stop(TYPECHECK, any.toString());
			Any token = ip.ostack.pop();
			Any post = ip.ostack.pop();
			if (!(token instanceof NumberType))
				throw new Stop(TYPECHECK, "cvr " + any);
			result = ((NumberType) token).realValue();
		} else {
			result = ((NumberType) any).realValue();
		}
		ip.ostack.pushRef(new RealType(result));
	}

	static void cvrs(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		int radix = ip.ostack.popInteger();
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		String result;
		if (radix == 10) {
			result = num.toString();
		} else {
			result = Integer.toString(num.intValue(), radix);
		}
		ip.ostack.pushRef(new StringType(ip.vm, string, result.toCharArray()));
	}

	static void cvs(Interpreter ip) {
		StringType string = (StringType) ip.ostack.pop(STRING);
		if (string.length() <= 0)
			throw new Stop(RANGECHECK);
		Any any = ip.ostack.pop();
		char val[];
		switch (any.typeID()) {
		case INTEGER:
		case REAL:
		case BOOLEAN:
		case NAME:
		case OPERATOR:
		case STRING:
			val = convert(any, string.length());
			break;
		default:
			val = convert(NOSTRINGVAL, NOSTRINGVAL.length());
			break;
		}
		ip.ostack.pushRef(new StringType(ip.vm, string, val));
	}

	private static char[] convert(Object obj, int n) {
		String sval = obj.toString();
		char array[] = new char[Math.min(n, sval.length())];
		sval.getChars(0, array.length, array, 0);
		return array;
	}

	static void executeonly(Interpreter ip) {
		Any any = ip.ostack.top(ARRAY | FILE | STRING);
		any.executeonly();
	}

	static void noaccess(Interpreter ip) {
		Any any = ip.ostack.top(ARRAY | DICT | FILE | STRING);
		any.noaccess();
	}

	static void readonly(Interpreter ip) {
		Any any = ip.ostack.top(ARRAY | DICT | FILE | STRING);
		any.readonly();
	}

	static void rcheck(Interpreter ip) {
		Any any = ip.ostack.pop(ARRAY | DICT | FILE | STRING);
		ip.ostack.pushRef(new BoolType(any.rcheck()));
	}

	static void wcheck(Interpreter ip) {
		Any any = ip.ostack.pop(ARRAY | DICT | FILE | STRING);
		ip.ostack.pushRef(new BoolType(any.wcheck()));
	}

}
