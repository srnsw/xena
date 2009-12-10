
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

import java.io.IOException;
import com.softhub.ps.util.CharStream;

public class BinaryCodec extends AbstractCodec {

	public static boolean debug;

	public final static int EEXEC_SEED = 55665;
	public final static int CHARSTRING_SEED = 4330;

	private final static int CRYPT_C1 = 52845;
	private final static int CRYPT_C2 = 22719;

	private int state;

	public BinaryCodec(int seed) {
		state = seed;
	}

	public int decode() throws IOException {
		int c = stream.getchar();
		return c < 0 ? -1 : decode(c);
	}

	public int decode(int c) {
		int val = c ^ (state >> 8);
		state = (((c + state) * CRYPT_C1) + CRYPT_C2) & 0xffff;
		if (debug) {
			print(val);
		}
		return val & 0xff;
	}

	public void encode(CharStream cs, int c) throws IOException {
		throw new IOException("BinaryCodec.encode not yet implemented");
	}

	private void print(int val) {
		char ch = (char) (val & 0xff);
		if (ch == '\n' || ch == '\r')
			lineno++;
		if (charno < 80) {
			System.out.print(ch);
		}
		if (charno++ >= 40 && (ch == ' ' || ch == '\r' || ch == '\n')) {
			System.out.print('\n');
			charno = 0;
		}
	}

	// for debugging only
	private int lineno = 1;
	private int charno = 0;

}
