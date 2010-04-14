
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

import com.softhub.ps.util.CharStream;
import java.io.IOException;

public abstract class CharSequenceType extends CompositeType
	implements CharStream
{
	/**
	 * Construct a character sequence object.
	 * @param vm the virtual memory
	 */
	public CharSequenceType(VM vm) {
		super(vm.isGlobal());
	}

	/**
	 * Construct a character sequence object.
	 * @param vm the virtual memory
	 * @param seq the object to copy
	 */
	public CharSequenceType(VM vm, CharSequenceType seq) {
		super(seq, vm.isGlobal());
	}

	/**
	 * Construct a character sequence object.
	 * @param seq the object to copy
	 */
	public CharSequenceType(CharSequenceType seq) {
		super(seq);
	}

	/**
	 * Read a token from this object and push
	 * it onto operand stack.
	 * @param ip the interpreter
	 * @return true if there are more tokens
	 */
	public abstract boolean token(Interpreter ip);

	/**
	 * Read a single character from this object.
	 * @return the character
	 */
	public abstract int getchar() throws IOException;

	/**
	 * Unread the character.
	 * @param c the character to be pushed back
	 */
	public abstract void ungetchar(int c) throws IOException;

	/**
	 * Write a single character to this object.
	 * @param c the character
	 */
	public abstract void putchar(int c) throws IOException;

	/**
	 * Close the stream.
	 */
	public abstract void close();

}
