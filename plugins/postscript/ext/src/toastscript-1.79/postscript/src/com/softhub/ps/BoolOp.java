
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

final class BoolOp extends OperatorType {

	private final static int AND = 1;
	private final static int OR = 2;
	private final static int XOR = 3;
	private final static int NOT = 4;
	private final static int BITSHIFT = 5;
	private final static int EQ = 6;
	private final static int NE = 7;
	private final static int LT = 8;
	private final static int LE = 9;
	private final static int GT = 10;
	private final static int GE = 11;

	static void install(Interpreter ip) {
		ip.installOp(new BoolOp(AND));
		ip.installOp(new BoolOp(OR));
		ip.installOp(new BoolOp(XOR));
		ip.installOp(new BoolOp(NOT));
		ip.installOp(new BoolOp(BITSHIFT));
		ip.installOp(new BoolOp(EQ));
		ip.installOp(new BoolOp(NE));
		ip.installOp(new BoolOp(LT));
		ip.installOp(new BoolOp(LE));
		ip.installOp(new BoolOp(GT));
		ip.installOp(new BoolOp(GE));
	}

	private int opcode;

	public BoolOp(int opcode) {
		super(toOpName(opcode));
		this.opcode = opcode;
	}

	public void exec(Interpreter ip) {
		switch (opcode) {
		case AND:
			and(ip);
			break;
		case OR:
			or(ip);
			break;
		case XOR:
			xor(ip);
			break;
		case NOT:
			not(ip);
			break;
		case BITSHIFT:
			bitshift(ip);
			break;
		case EQ:
			eq(ip);
			break;
		case NE:
			ne(ip);
			break;
		case LT:
			lt(ip);
			break;
		case LE:
			le(ip);
			break;
		case GT:
			gt(ip);
			break;
		case GE:
			ge(ip);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static String toOpName(int opcode) {
		switch (opcode) {
		case AND:
			return "and";
		case OR:
			return "or";
		case XOR:
			return "xor";
		case NOT:
			return "not";
		case BITSHIFT:
			return "bitshift";
		case EQ:
			return "eq";
		case NE:
			return "ne";
		case LT:
			return "lt";
		case LE:
			return "le";
		case GT:
			return "gt";
		case GE:
			return "ge";
		default:
			throw new IllegalArgumentException();
		}
	}

	private static void and(Interpreter ip) {
		Any b = ip.ostack.pop(BOOLEAN | INTEGER);
		Any a = ip.ostack.pop(BOOLEAN | INTEGER);
		if (a.typeCode() != b.typeCode())
			throw new Stop(TYPECHECK);
		int result = ((NumberType) a).intValue() & ((NumberType) b).intValue();
		ip.ostack.pushRef(a instanceof BoolType ? ((Any) new BoolType(result)) : ((Any) new IntegerType(result)));
	}

	private static void or(Interpreter ip) {
		Any b = ip.ostack.pop(BOOLEAN | INTEGER);
		Any a = ip.ostack.pop(BOOLEAN | INTEGER);
		if (a.typeCode() != b.typeCode())
			throw new Stop(TYPECHECK);
		int result = ((NumberType) a).intValue() | ((NumberType) b).intValue();
		ip.ostack.pushRef(a instanceof BoolType ? ((Any) new BoolType(result)) : ((Any) new IntegerType(result)));
	}

	private static void xor(Interpreter ip) {
		Any b = ip.ostack.pop(BOOLEAN | INTEGER);
		Any a = ip.ostack.pop(BOOLEAN | INTEGER);
		if (a.typeCode() != b.typeCode())
			throw new Stop(TYPECHECK);
		int result = ((NumberType) a).intValue() ^ ((NumberType) b).intValue();
		ip.ostack.pushRef(a instanceof BoolType ? ((Any) new BoolType(result)) : ((Any) new IntegerType(result)));
	}

	private static void not(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(BOOLEAN | INTEGER);
		ip.ostack.pushRef(num instanceof BoolType ? ((Any) new BoolType(!((BoolType) num).booleanValue())) : ((Any) new IntegerType(~num.intValue())));
	}

	private static void bitshift(Interpreter ip) {
		int b = ip.ostack.popInteger();
		int a = ip.ostack.popInteger();
		ip.ostack.pushRef(new IntegerType(b >= 0 ? a << b : a >>> -b));
	}

	private static void eq(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() == ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(a.equals(b)));
		} else {
			ip.ostack.pushRef(new BoolType(a.equals(b)));
		}
	}

	private static void ne(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() != ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(!a.equals(b)));
		} else {
			ip.ostack.pushRef(new BoolType(!a.equals(b)));
		}
	}

	private static void lt(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() < ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(a.toString().compareTo(b.toString()) < 0));
		} else
			throw new Stop(TYPECHECK);
	}

	private static void le(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() <= ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(a.toString().compareTo(b.toString()) <= 0));
		} else
			throw new Stop(TYPECHECK);
	}

	private static void gt(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() > ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(a.toString().compareTo(b.toString()) > 0));
		} else
			throw new Stop(TYPECHECK);
	}

	private static void ge(Interpreter ip) {
		Any b = ip.ostack.pop();
		Any a = ip.ostack.pop();
		if ((a instanceof NumberType) && (b instanceof NumberType)) {
			ip.ostack.pushRef(new BoolType(((NumberType) a).realValue() >= ((NumberType) b).realValue()));
		} else if ((a instanceof StringType) && (b instanceof StringType)) {
			ip.ostack.pushRef(new BoolType(a.toString().compareTo(b.toString()) >= 0));
		} else
			throw new Stop(TYPECHECK);
	}

}
