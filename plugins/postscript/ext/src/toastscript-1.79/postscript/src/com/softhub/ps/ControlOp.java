
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

import com.softhub.ps.filter.EExecCodecFactory;
import com.softhub.ps.filter.Codec;
import java.io.IOException;
import java.util.Enumeration;

final class ControlOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"stop", "exec", "eexec", "ifelse", "exit", "execstack"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, ControlOp.class);
		ip.installOp(new StoppedOp());
		ip.installOp(new IfOp());
		ip.installOp(new LoopOp());
		ip.installOp(new ForallOp());
		ip.installOp(new RepeatOp());
		ip.installOp(new ForOp());
	}

	static void stop(Interpreter ip) {
		int count = ip.estack.countto(Any.STOPPEDCONTEXT);
		if (count < ip.estack.count()) {
			ip.estack.remove(count+1);
			ip.ostack.push(BoolType.TRUE);
		} else {
			// this should never happen
			ip.estack.pushRef(new QuitOp());
		}
	}

	static void exec(Interpreter ip) {
		Any any = ip.ostack.pop();
		switch (any.typeID()) {
		case FILE:
		case ARRAY:
		case NAME:
		case STRING:
		case OPERATOR:
			if (any.isExecutable()) {
				ip.estack.push(any);
				break;
			}
			// fall through
		default:
			ip.ostack.pushRef(any);
			break;
		}
	}

	static void eexec(Interpreter ip) {
		CharSequenceType cs = (CharSequenceType) ip.ostack.pop(STRING | FILE);
		if (cs instanceof FilterType) {
			cs = ((FilterType) cs).getSourceStream();
		}
		Codec codec;
		try {
		    codec = EExecCodecFactory.createCodec(cs, FilterType.READ_MODE);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
		FilterType filter = new FilterType(ip.vm, cs, codec, FilterType.READ_MODE);
		filter.cvx();
		ip.estack.pushRef(new EExecActionOp(filter));
		ip.dstack.pushRef(ip.systemdict);
	}

	static class EExecActionOp extends OperatorType {

		private FilterType filter;

		EExecActionOp(FilterType filter) {
			super("eexec");
			this.filter = filter;
		}

		public void exec(Interpreter ip) {
			if (filter.isClosed()) {
				if (ip.dstack.top() == ip.systemdict) {
					ip.dstack.pop();
				}
			} else {
			    ip.estack.pushRef(this);
			    ip.estack.pushRef(filter);
			}
		}

	}

	static void execstack(Interpreter ip) {
		ArrayType a = (ArrayType) ip.ostack.pop(ARRAY);
		for (int i = 0; i < ip.estack.count(); i++)
			a.put(ip.vm, i, (Any) ip.estack.elementAt(i));
		ip.ostack.pushRef(a);
	}

	static void ifelse(Interpreter ip) {
		ArrayType b = (ArrayType) ip.ostack.pop(ARRAY);
		ArrayType a = (ArrayType) ip.ostack.pop(ARRAY);
		if (a.isLiteral() || b.isLiteral())
			throw new Stop(TYPECHECK);
		boolean cond = ip.ostack.popBoolean();
		ip.estack.pushRef(cond ? a : b);
	}

	static void exit(Interpreter ip) {
		int index = ip.estack.countto(Any.LOOPCONTEXT);
		if (index >= ip.estack.count())
			throw new Stop(INVALIDEXIT);
		Any context = ip.estack.index(index);
		if (context instanceof StoppedOp) {
			ip.ostack.push(BoolType.TRUE);
		}
		ip.estack.remove(index+1);
	}

	static class StoppedOp extends OperatorType {

		StoppedOp() {
			super("stopped");
		}

		public int typeCode() {
			return OPERATOR | STOPPEDCONTEXT | LOOPCONTEXT;
		}

		public void exec(Interpreter ip) {
			Any any = ip.ostack.pop();
			int type = any.typeCode();
			if (type == ARRAY && any.isLiteral())
				throw new Stop(TYPECHECK);
			ip.estack.pushRef(new StoppedActionOp());
			ip.estack.pushRef(any);
		}

	}

	final static class StoppedActionOp extends StoppedOp {

		StoppedActionOp() {
			super();
		}

		public void exec(Interpreter ip) {
			ip.ostack.push(BoolType.FALSE);
		}

	}

	static class IfOp extends OperatorType {

		IfOp() {
			super("if");
		}

		public void exec(Interpreter ip) {
			ArrayType a = (ArrayType) ip.ostack.pop(ARRAY);
			if (a.isLiteral())
				throw new Stop(TYPECHECK);
			boolean cond = ip.ostack.popBoolean();
			if (cond) {
				ip.estack.pushRef(a);
			}
		}

	}

	static class LoopOp extends OperatorType {

		LoopOp() {
			super("loop");
		}

		public int typeCode() {
			return OPERATOR | LOOPCONTEXT;
		}

		public void exec(Interpreter ip) {
			ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
			if (proc.isLiteral())
				throw new Stop(TYPECHECK);
			ip.estack.pushRef(new LoopActionOp(proc));
		}

	}

	final static class LoopActionOp extends LoopOp {

		private ArrayType proc;

		LoopActionOp(ArrayType proc) {
			super();
			this.proc = proc;
		}

		public void exec(Interpreter ip) {
			ip.estack.pushRef(this);
			ip.estack.push(proc);
		}

	}

	static class ForallOp extends OperatorType {

		ForallOp() {
			super("forall");
		}

		public int typeCode() {
			return OPERATOR | LOOPCONTEXT;
		}

		public void exec(Interpreter ip) {
			ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
			if (proc.isLiteral())
				throw new Stop(TYPECHECK);
			Any any = (Any) ip.ostack.pop(ARRAY | DICT | STRING);
			Enumerable container = (Enumerable) any;
			switch (any.typeID()) {
			case ARRAY:
			case STRING:
				ip.estack.pushRef(new ArrayForallActionOp(proc, container));
				break;
			case DICT:
				ip.estack.pushRef(new DictForallActionOp(proc, container));
				break;
			}
		}

	}

	final static class ArrayForallActionOp extends ForallOp {

		private ArrayType proc;
		private Enumeration	iterator;

		ArrayForallActionOp(ArrayType proc, Enumerable container) {
			super();
			this.proc = proc;
			iterator = container.elements();
		}

		public void exec(Interpreter ip) {
			if (iterator.hasMoreElements()) {
				ip.ostack.pushRef((Any) iterator.nextElement());
				ip.estack.pushRef(this);
				ip.estack.push(proc);
			}
		}

	}

	final static class DictForallActionOp extends ForallOp {

		private ArrayType proc;
		private Enumeration	keyIterator;
		private Enumeration	valIterator;

		DictForallActionOp(ArrayType proc, Enumerable container) {
			super();
			this.proc = proc;
			keyIterator = container.keys();
			valIterator = container.elements();
		}

		public void exec(Interpreter ip) {
			if (valIterator.hasMoreElements()) {
				ip.ostack.pushRef((Any) keyIterator.nextElement());
				ip.ostack.pushRef((Any) valIterator.nextElement());
				ip.estack.pushRef(this);
				ip.estack.push(proc);
			}
		}

	}

	static class RepeatOp extends OperatorType {

		RepeatOp() {
			super("repeat");
		}

		public int typeCode() {
			return OPERATOR | LOOPCONTEXT;
		}

		public void exec(Interpreter ip) {
			ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
			if (proc.isLiteral())
				throw new Stop(TYPECHECK);
			int count = ip.ostack.popInteger();
			if (count < 0)
				throw new Stop(RANGECHECK);
			ip.estack.pushRef(new RepeatActionOp(proc, count));
		}

	}

	final static class RepeatActionOp extends RepeatOp {

		private ArrayType proc;
		private int count;
		private int maxCount;

		RepeatActionOp(ArrayType proc, int maxCount) {
			super();
			this.proc = proc;
			this.maxCount = maxCount;
		}

		public void exec(Interpreter ip) {
			if (count++ < maxCount) {
				ip.estack.pushRef(this);
				ip.estack.push(proc);
			}
		}

	}

	static class ForOp extends OperatorType {

		ForOp() {
			super("for");
		}

		public int typeCode() {
			return OPERATOR | LOOPCONTEXT;
		}

		public void exec(Interpreter ip) {
			ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
			if (proc.isLiteral())
				throw new Stop(TYPECHECK);
			NumberType maxcount = (NumberType) ip.ostack.pop(NUMBER);
			NumberType increment = (NumberType) ip.ostack.pop(NUMBER);
			NumberType initial = (NumberType) ip.ostack.pop(NUMBER);
			if (increment.floatValue() == 0)
				throw new Stop(UNDEFINED);
			ip.estack.pushRef(new ForActionOp(proc, initial, increment, maxcount));
		}

	}

	final static class ForActionOp extends ForOp {

		private ArrayType proc;
		private NumberType initial;
		private NumberType increment;
		private NumberType maxCount;

		ForActionOp(ArrayType proc, NumberType initial, NumberType increment, NumberType maxCount) {
			super();
			this.proc = proc;
			this.initial = initial;
			this.increment = increment;
			this.maxCount = maxCount;
		}

		public void exec(Interpreter ip) {
			if (increment.realValue() >= 0) {
				if (initial.realValue() <= maxCount.realValue()) {
					ip.ostack.pushRef(initial);
					ip.estack.pushRef(this);
					ip.estack.push(proc);
					initial = ArithOp.add(initial, increment);
				} else {
					proc = null;
				}
			} else {
				if (initial.realValue() >= maxCount.realValue()) {
					ip.ostack.pushRef(initial);
					ip.estack.pushRef(this);
					ip.estack.push(proc);
					initial = ArithOp.add(initial, increment);
				} else {
					proc = null;
				}
			}
		}

	}

	final static class QuitOp extends OperatorType {

		QuitOp() {
			super("quit");
		}

		public int typeCode() {
			return OPERATOR | QUITCONTEXT;
		}

		public void exec(Interpreter ip) {
			System.out.println("Good bye!");
			int count = ip.estack.countto(QUITCONTEXT);
			if (count > 0) {
				ip.estack.remove(count);
			} else {
				ip.estack.remove(ip.estack.count());
			}
		}

	}

}
