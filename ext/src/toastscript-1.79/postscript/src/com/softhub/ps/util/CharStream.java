
package com.softhub.ps.util;

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

import java.io.IOException;

public interface CharStream {

	/**
	 * Read mode constant.
	 */
	public final static int	READ_MODE = 1;

	/**
	 * Write mode constant.
	 */
	public final static int	WRITE_MODE = 2;

	/**
	 * @return current character from stream
	 */
	int getchar() throws IOException;

	/**
	 * @param c the character to be written to stream
	 */
	void putchar(int c) throws IOException;

	/**
	 * Push back character to be read again at next
	 * call to getchar.
	 * @param c the character to be pushed
	 */
	void ungetchar(int c) throws IOException;

	/**
	 * Close the character stream.
	 */
	void close() throws IOException;

}
