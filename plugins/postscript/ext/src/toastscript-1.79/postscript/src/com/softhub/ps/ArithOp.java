
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
 *
 * Arithmetic operators are used quite frequently, so we
 * do some optimization on them. A switch block appears to
 * be about 20% faster than Method.invoke but implementing
 * each arithmetic operator in its own class would make it
 * about 40% faster. Using one class for each operator
 * increases the size of the jar file quite a bit. These are
 * tradeoffs to be considered.
 */

import java.util.Random;

final class ArithOp extends OperatorType {

	private final static double LN10 = Math.log(10);

	private static Random random = new Random(0);

	private static final int ADD = 1;
	private static final int SUB = 2;
	private static final int MUL = 3;
	private static final int DIV = 4;
	private static final int IDIV = 5;
	private static final int MOD = 6;
	private static final int ABS = 7;
	private static final int NEG = 8;
	private static final int SQRT = 9;
	private static final int CEILING = 10;
	private static final int FLOOR = 11;
	private static final int ROUND = 12;
	private static final int TRUNCATE = 13;
	private static final int EXP = 14;
	private static final int LN = 15;
	private static final int LOG = 16;
	private static final int ATAN = 17;
	private static final int SIN = 18;
	private static final int COS = 19;
	private static final int RAND = 20;
	private static final int SRAND = 21;
	private static final int RRAND = 22;

	static void install(Interpreter ip) {
		ip.installOp(new ArithOp(ADD));
		ip.installOp(new ArithOp(SUB));
		ip.installOp(new ArithOp(MUL));
		ip.installOp(new ArithOp(DIV));
		ip.installOp(new ArithOp(IDIV));
		ip.installOp(new ArithOp(MOD));
		ip.installOp(new ArithOp(ABS));
		ip.installOp(new ArithOp(NEG));
		ip.installOp(new ArithOp(SQRT));
		ip.installOp(new ArithOp(CEILING));
		ip.installOp(new ArithOp(FLOOR));
		ip.installOp(new ArithOp(ROUND));
		ip.installOp(new ArithOp(TRUNCATE));
		ip.installOp(new ArithOp(EXP));
		ip.installOp(new ArithOp(LN));
		ip.installOp(new ArithOp(LOG));
		ip.installOp(new ArithOp(ATAN));
		ip.installOp(new ArithOp(SIN));
		ip.installOp(new ArithOp(COS));
		ip.installOp(new ArithOp(RAND));
		ip.installOp(new ArithOp(SRAND));
		ip.installOp(new ArithOp(RRAND));
	}

	private int opcode;

	public ArithOp(int opcode) {
		super(toOpName(opcode));
		this.opcode = opcode;
	}

	public void exec(Interpreter ip) {
		switch (opcode) {
		case ADD:
			add(ip);
			break;
		case SUB:
			sub(ip);
			break;
		case MUL:
			mul(ip);
			break;
		case DIV:
			div(ip);
			break;
		case IDIV:
			idiv(ip);
			break;
		case MOD:
			mod(ip);
			break;
		case ABS:
			abs(ip);
			break;
		case NEG:
			neg(ip);
			break;
		case SQRT:
			sqrt(ip);
			break;
		case CEILING:
			ceiling(ip);
			break;
		case FLOOR:
			floor(ip);
			break;
		case ROUND:
			round(ip);
			break;
		case TRUNCATE:
			truncate(ip);
			break;
		case EXP:
			exp(ip);
			break;
		case LN:
			ln(ip);
			break;
		case LOG:
			log(ip);
			break;
		case ATAN:
			atan(ip);
			break;
		case SIN:
			sin(ip);
			break;
		case COS:
			cos(ip);
			break;
		case RAND:
			rand(ip);
			break;
		case SRAND:
			srand(ip);
			break;
		case RRAND:
			rrand(ip);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static String toOpName(int opcode) {
		switch (opcode) {
		case ADD:
			return "add";
		case SUB:
			return "sub";
		case MUL:
			return "mul";
		case DIV:
			return "div";
		case IDIV:
			return "idiv";
		case MOD:
			return "mod";
		case ABS:
			return "abs";
		case NEG:
			return "neg";
		case SQRT:
			return "sqrt";
		case CEILING:
			return "ceiling";
		case FLOOR:
			return "floor";
		case ROUND:
			return "round";
		case TRUNCATE:
			return "truncate";
		case EXP:
			return "exp";
		case LN:
			return "ln";
		case LOG:
			return "log";
		case ATAN:
			return "atan";
		case SIN:
			return "sin";
		case COS:
			return "cos";
		case RAND:
			return "rand";
		case SRAND:
			return "srand";
		case RRAND:
			return "rrand";
		default:
			throw new IllegalArgumentException();
		}
	}

	static void add(Interpreter ip) {
		NumberType b = (NumberType) ip.ostack.pop(NUMBER);
		NumberType a = (NumberType) ip.ostack.pop(NUMBER);
		ip.ostack.pushRef(add(a, b));
	}

	static NumberType add(NumberType a, NumberType b) {
		if (a.isReal() || b.isReal())
			return new RealType(a.realValue() + b.realValue());
		else
			return new IntegerType(a.intValue() + b.intValue());
	}

	static void sub(Interpreter ip) {
		NumberType b = (NumberType) ip.ostack.pop(NUMBER);
		NumberType a = (NumberType) ip.ostack.pop(NUMBER);
		ip.ostack.pushRef(sub(a, b));
	}

	static NumberType sub(NumberType a, NumberType b) {
		if (a.isReal() || b.isReal())
			return new RealType(a.realValue() - b.realValue());
		else
			return new IntegerType(a.intValue() - b.intValue());
	}

	static void mul(Interpreter ip) {
		NumberType b = (NumberType) ip.ostack.pop(NUMBER);
		NumberType a = (NumberType) ip.ostack.pop(NUMBER);
		ip.ostack.pushRef(mul(a, b));
	}

	static NumberType mul(NumberType a, NumberType b) {
		if (a.isReal() || b.isReal())
			return new RealType(a.realValue() * b.realValue());
		else
			return new IntegerType(a.intValue() * b.intValue());
	}

	static void div(Interpreter ip) {
		double b = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double a = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		ip.ostack.pushRef(new RealType(a / b));
	}

	static void idiv(Interpreter ip) {
		int b = ip.ostack.popInteger();
		if (b == 0)
			throw new Stop(UNDEFINEDRESULT);
		int a = ip.ostack.popInteger();
		ip.ostack.pushRef(new IntegerType(a / b));
	}

	static void mod(Interpreter ip) {
		int b = ip.ostack.popInteger();
		if (b == 0)
			throw new Stop(UNDEFINEDRESULT);
		int a = ip.ostack.popInteger();
		ip.ostack.pushRef(new IntegerType(a % b));
	}

	static void abs(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(Math.abs(num.realValue())));
		else
			ip.ostack.pushRef(new IntegerType(Math.abs(num.intValue())));
	}

	static void neg(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(-num.realValue()));
		else
			ip.ostack.pushRef(new IntegerType(-num.intValue()));
	}

	static void sqrt(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		ip.ostack.pushRef(new RealType(Math.sqrt(num.realValue())));
	}

	static void ceiling(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(Math.ceil(num.realValue())));
		else
			ip.ostack.pushRef(num);
	}

	static void floor(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(Math.floor(num.realValue())));
		else
			ip.ostack.pushRef(num);
	}

	static void round(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(Math.round(num.realValue())));
		else
			ip.ostack.pushRef(num);
	}

	static void truncate(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		if (num.isReal())
			ip.ostack.pushRef(new RealType(Math.rint(num.realValue())));
		else
			ip.ostack.pushRef(num);
	}

	static void exp(Interpreter ip) {
		double exponent = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double base = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		try {
			ip.ostack.pushRef(new RealType(Math.pow(base, exponent)));
		} catch (ArithmeticException e) {
			throw new Stop(UNDEFINEDRESULT);
		}
	}

	static void ln(Interpreter ip) {
		double num = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		if (num <= 0)
			throw new Stop(RANGECHECK);
		ip.ostack.pushRef(new RealType(Math.log(num)));
	}

	static void log(Interpreter ip) {
		double num = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		if (num <= 0)
			throw new Stop(RANGECHECK);
		ip.ostack.pushRef(new RealType(Math.log(num) / LN10));
	}

	static void atan(Interpreter ip) {
		double den = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		double num = ((NumberType) ip.ostack.pop(NUMBER)).realValue();
		if (num == 0 && den == 0)
			throw new Stop(UNDEFINEDRESULT);
		ip.ostack.pushRef(new RealType(Math.atan2(num, den) / Math.PI * 180));
	}

	static void sin(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		double val = num.realValue() * Math.PI / 180;
		ip.ostack.pushRef(new RealType(Math.sin(val)));
	}

	static void cos(Interpreter ip) {
		NumberType num = (NumberType) ip.ostack.pop(NUMBER);
		double val = num.realValue() * Math.PI / 180;
		ip.ostack.pushRef(new RealType(Math.cos(val)));
	}

	static synchronized void rand(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(Math.abs(random.nextInt())));
	}

	static synchronized void srand(Interpreter ip) {
		random.setSeed(((NumberType) ip.ostack.pop(NUMBER)).intValue());
	}

	static synchronized void rrand(Interpreter ip) {
		ip.ostack.pushRef(new RealType(random.nextDouble()));
	}

}
