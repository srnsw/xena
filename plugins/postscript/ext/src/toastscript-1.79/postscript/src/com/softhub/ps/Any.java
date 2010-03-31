
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

public abstract class Any implements Cloneable, Stoppable, Types {

	public final static int TYPEMASK = 0x0000FFFF;
	public final static int STOPPEDCONTEXT = 1<<16;
	public final static int LOOPCONTEXT = 1<<17;
	public final static int QUITCONTEXT = 1<<18;

	final static int EXEC_BIT    = 1;
	final static int RMODE_BIT   = 2;
	final static int WMODE_BIT   = 4;
	final static int XMODE_BIT   = 8;
	final static int GLOBAL_BIT  = 16;
	final static int PACKED_BIT  = 32;
	final static int BIND_BIT    = 64;
	final static int LINENOSHIFT = 10;
	final static int LINENOMASK  = -1 << LINENOSHIFT;

	/**
	 * Basic object flags.
	 */
	private int	flags = RMODE_BIT | WMODE_BIT | XMODE_BIT | LINENOMASK;

	/**
	 * Construct a new object.
	 */
	public Any() {
	}

	/**
	 * Construct a new object.
	 * @param any the template
	 */
	public Any(Any any) {
		flags = any.flags;
	}

	/**
	 * @return a clone of the object
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return a type code for this object
	 */
	public abstract int typeCode();

	/**
	 * @return a type name for this object
	 */
	public abstract String typeName();

	/**
	 * @return java representation
	 */
	public Object cvj() {
		throw new Stop(TYPECHECK);
	}

	/**
	 * @return the type id
	 */
	public int typeID() {
		return typeCode() & TYPEMASK;
	}

	/**
	 * @param bits the type mask
	 * @return true if this object matches this type
	 */
	public boolean typeOf(int bits) {
		return (typeCode() & bits) != 0;
	}

	/**
	 * Execute the object.
	 * @param ip the ps-interpreter
	 */
	public void exec(Interpreter ip) {
		ip.ostack.push(this);
	}

	/**
	 * Temporarily overwrite the access attributes.
	 * @return the access attributes
	 */
	public int saveAccessFlags() {
		int save = this.flags;
		this.flags |= RMODE_BIT | WMODE_BIT | XMODE_BIT;
		return save;
	}

	/**
	 * Restore the access attributes.
	 * @param flags the access attributes
	 */
	public void restoreAccessFlags(int flags) {
		this.flags = flags;
	}

	public boolean isLiteral() {
		return (flags & EXEC_BIT) == 0;
	}

	public boolean isExecutable() {
		return (flags & EXEC_BIT) != 0;
	}

	public Any cvx() {
		flags |= EXEC_BIT;
		return this;
	}

	public Any cvlit() {
		flags &= ~EXEC_BIT;
		return this;
	}

	public boolean rcheck() {
		return (flags & RMODE_BIT) != 0;
	}

	public boolean wcheck() {
		return (flags & WMODE_BIT) != 0;
	}

	public Any noaccess() {
		flags &= ~(WMODE_BIT | RMODE_BIT | XMODE_BIT);
		return this;
	}

	public Any executeonly() {
		flags &= ~(WMODE_BIT | RMODE_BIT);
		return this;
	}

	public Any readonly() {
		flags &= ~WMODE_BIT;
		return this;
	}

	protected void setBound() {
		flags &= ~WMODE_BIT;
		flags |= BIND_BIT;
	}

	protected boolean isBound() {
		return (flags & BIND_BIT) != 0;
	}

	protected void setGlobal() {
		flags |= GLOBAL_BIT;
	}

	protected boolean isGlobal() {
		return (flags & GLOBAL_BIT) != 0;
	}

	protected void setPacked(boolean val) {
		if (val) {
			flags |= PACKED_BIT;
//			flags &= ~WMODE_BIT;	// this breaks "bind"
		} else {
			flags &= ~PACKED_BIT;
		}
	}

	protected boolean isPacked() {
		return (flags & PACKED_BIT) != 0;
	}

	protected void setLineNo(int no) {
		flags = (flags & ~LINENOMASK) | (no << LINENOSHIFT);
	}

	protected void setLineNo(Any any) {
		flags = (flags & ~LINENOMASK) | (any.flags & LINENOMASK);
	}

	public int getLineNo() {
		return flags >> LINENOSHIFT;
	}

}
