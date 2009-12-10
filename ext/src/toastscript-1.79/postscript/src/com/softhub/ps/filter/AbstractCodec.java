
package com.softhub.ps.filter;

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

public abstract class AbstractCodec implements Codec {

	protected int mode;
	protected CharStream stream;
	protected boolean endOfData;

	public void open(CharStream stream, int mode) throws IOException {
		this.stream = stream;
		this.mode = mode;
	}

	public void close() throws IOException {
	}

	public int decode() throws IOException {
		throw new IOException("not implemented");
	}

	public void encode(int c) throws IOException {
		throw new IOException("not implemented");
	}

	public Class[] getOptionalParameterTypes() {
		return null;
	}

	public void setOptionalParameters(Object array[]) {
	}

	protected static boolean isWhiteSpace(int c) {
		switch (c) {
		case '\0':
		case '\n':
		case '\r':
		case '\f':
		case '\t':
		case ' ':
			return true;
		}
		return false;
	}

}
