
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

public class RunLengthCodec extends AbstractCodec {

	private static int cnt;

	private char buffer[] = new char[128];
	private int count;

	public int decode() throws IOException {
		if (endOfData)
			return EOD;
		if (count <= 0) {
			fillDecodeBuffer();
			if (endOfData)
				return EOD;
		}
		return buffer[--count];
	}

	public void encode(int c) throws IOException {
		throw new IOException("RunLengthCodec.encode not yet implemented");
	}

	private void fillDecodeBuffer() throws IOException {
		int i, n = stream.getchar();
		if (n < 0 || n > 255)
			throw new IOException("expecting length byte");
		if (n < 128) {
			count = n + 1;
			for (i = n; i >= 0; i--) {
				int c = stream.getchar();
				if (c < 0)
					throw new IOException("unexpected end of sequence: " + i + ", " + n);
				buffer[i] = (char) c;
			}
		} else if (n > 128) {
			count = 257 - n;
			int c = stream.getchar();
			if (c < 0)
				throw new IOException("unexpected end of replication");
			char cc = (char) c;
			for (i = count-1; i >= 0; i--) {
				buffer[i] = cc;
			}
		} else {
			endOfData = true;
		}
		if (!endOfData) {
			// look ahead for end of filter sequence
			int c = stream.getchar();
			if (c == 128) {
				endOfData = true;
			} else {
				stream.ungetchar(c);
			}
		}
	}

}
