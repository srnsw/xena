
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

public class ExStack extends Stack {

	final static int YIELD_COUNT = 5000;

	private int yieldCount;
	private int ostackcount;
	private Any currentobject;
	private boolean interrupted;

	/**
	 * Construct an execution stack.
	 * @param size the size of the stack
	 */
	public ExStack(int size) {
		super(size);
	}

	/**
	 * Run the PostScript engine until the execution stack
	 * is empty.
	 * @param ip the interpreter
	 */
	public void run(Interpreter ip) {
		run(ip, 0);
	}

	/**
	 * Push an object onto the execution stack and run the
	 * PostScript engine until the object is popped off.
	 * This is used for recursive invocation by some operators
	 * which expect a procedure parameter.
	 * @param ip the interpreter
	 * @param any the object to execute
	 */
	public void run(Interpreter ip, Any any) {
		Any tmp = currentobject;
		push(any);
		run(ip, count-1);
		currentobject = tmp;
	}

	/**
	 * Run the PostScript engine until the execution stack
	 * reaches estackcount.
	 * @param ip the interpreter
	 */
	private void run(Interpreter ip, int estackcount) {
		do {
			try {
				runSave(ip, estackcount);
			} catch (Stop ex) {
				switch (ex.getExceptionId()) {
				case EXSTACKOVERFLOW:
					// remove topmost element to make
					// space for the stop object
					remove(1);
					break;
				case DICTSTACKOVERFLOW:
					// remove 2 elements from dict stack to make
					// space for dictionaries the error handler
					// will push
					ip.dstack.remove(2);
					// fall through
				default:
					// push the object which caused the error
					// onto execution stack
					pushRef(currentobject);
				}
				// restore the operand stack to the state
				// before the error occured
				ip.ostack.count = ostackcount;
				// record the error
				ex.recorderror(ip);
				// execute the stop operator
				push(ip.systemdict.get("stop"));
			}
		} while (count > estackcount);
	}

	/**
	 * Execute the objects on the stack until index 'estackcount'
	 * is reached or and error occured.
	 * @param ip the interpreter
	 * @param estackcount the 'low water mark'
	 */
	private void runSave(Interpreter ip, int estackcount) {
		try {
			while (count > estackcount) {
				ostackcount = ip.ostack.count;
				currentobject = array[--count];
				array[count] = null;
				if (yieldCount++ >= YIELD_COUNT) {
					if (interrupted) {
						interrupted = false;
						throw new Stop(INTERRUPT);
					}
					Thread.yield();
					yieldCount = 0;
				}
				currentobject.exec(ip);
			}
		} catch (OutOfMemoryError ex) {
			System.gc();
			throw new Stop(INTERNALERROR, "out of memory");
		} catch (Stop ex) {
			throw ex;
		} catch (Throwable ex) {
			System.err.println("internal error in " + currentobject);
			throw new Stop(INTERNALERROR, ex.getMessage());
		}
	}

	void interrupt(boolean state) {
		interrupted = state;
	}

	protected int overflow() {
		return EXSTACKOVERFLOW;
	}

	protected int underflow() {
		return INTERNALERROR;
	}

}
