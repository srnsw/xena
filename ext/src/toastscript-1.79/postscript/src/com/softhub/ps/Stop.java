
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

public class Stop extends RuntimeException implements Stoppable {

	/**
	 * The exception id.
	 */
	private int cause;

	/**
	 * Construct an exception which causes the interpreter to stop execution.
	 * @param cause the exception id
	 */
	public Stop(int cause) {
		super();
		this.cause = cause;
	}

	/**
	 * Construct an exception which causes the interpreter to stop execution.
	 * @param cause the exception id
	 * @param msg the detail message
	 */
	public Stop(int cause, String msg) {
		super(msg);
		this.cause = cause;
	}

	/**
	 * Get the cause for this exception.
	 * @return the cause
	 */
	public int getExceptionId() {
		return cause;
	}

	/**
	 * Handle the exception.
	 * @param ip the interpreter
	 */
	public void recorderror(Interpreter ip) {
		DictType serror = (DictType) ip.systemdict.get("$error");
		boolean global = ip.vm.isGlobal();
		if (global) {
			// reset VM allocation mode to local
			ip.vm.setGlobal(false);
		}
		// capture the state
		if (serror != null) {
			Any cmd = ip.estack.top();
			String details = getMessage();
			// record information about error
			serror.put(ip.vm, "newerror", BoolType.TRUE);
			serror.put(ip.vm, "errorname", new NameType(getName()));
			serror.put(ip.vm, "command", cmd);
			serror.put(ip.vm, "errorinfo", new ArrayType(ip.vm, 0));	// TODO: implement this
			serror.put(ip.vm, "ostack", new ArrayType(ip.vm, ip.ostack.count(), ip.ostack));
			serror.put(ip.vm, "estack", new ArrayType(ip.vm, ip.estack.count(), ip.estack));
			serror.put(ip.vm, "dstack", new ArrayType(ip.vm, ip.dstack.count(), ip.dstack));
			serror.put(ip.vm, "recordstacks", BoolType.TRUE);			// TODO: Display PostScript
			serror.put(ip.vm, "binary", BoolType.FALSE);				// TODO: Display PostScript
			serror.put(ip.vm, "details", new NameType(details != null ? details : "none"));
			serror.put(ip.vm, "global", new BoolType(global));
			serror.put(ip.vm, "lineno", new IntegerType(cmd.getLineNo()));
			serror.put(ip.vm, "currentline", new IntegerType(ip.lineno));
			serror.put(ip.vm, "currentfilename", new StringType(ip.vm, FileOp.getCurrentFile(ip).getName()));
		}
	}

	public String getName() {
		switch (cause) {
		case TYPECHECK:
			return "typecheck";
		case STACKUNDERFLOW:
			return "stackunderflow";
		case STACKOVERFLOW:
			return "stackoverflow";
		case EXSTACKOVERFLOW:
			return "execstackoverflow";
		case DICTSTACKOVERFLOW:
			return "dictstackoverflow";
		case DICTSTACKUNDERFLOW:
			return "dictstackunderflow";
		case UNDEFINED:
			return "undefined";
		case UNDEFINEDRESULT:
			return "undefinedresult";
		case RANGECHECK:
			return "rangecheck";
		case UNMATCHEDMARK:
			return "unmatchedmark";
		case LIMITCHECK:
			return "limitcheck";
		case SYNTAXERROR:
			return "syntaxerror";
		case INVALIDACCESS:
			return "invalidaccess";
		case INVALIDEXIT:
			return "invalidexit";
		case INVALIDRESTORE:
			return "invalidrestore";
		case UNDEFINEDFILENAME:
			return "undefinedfilename";
		case UNDEFINEDRESOURCE:
			return "undefinedresource";
		case INVALIDFILEACCESS:
			return "invalidfileaccess";
		case INVALIDFONT:
			return "invalidfont";
		case IOERROR:
			return "ioerror";
		case INTERRUPT:
			return "interrupt";
		case NOCURRENTPOINT:
			return "nocurrentpoint";
		case SECURITYCHECK:
			return "securitycheck";
		case TIMEOUT:
			return "timeout";
		case INTERNALERROR:
			return "internalerror";
		default:
			System.out.println("errcode: " + cause);
			return "unknownerror";
		}
	}

	public String toString() {
		return getName();
	}

}
