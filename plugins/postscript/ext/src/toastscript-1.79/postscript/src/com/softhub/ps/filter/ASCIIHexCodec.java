
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

public class ASCIIHexCodec extends AbstractCodec {

	private int charCounter;

	public void close() throws IOException {
		if (mode == CharStream.WRITE_MODE) {
		    stream.putchar('>');
		}
	}

	public int decode() throws IOException {
		if (endOfData)
			return EOD;
		int c, c0, c1;
		while ((c = stream.getchar()) >= 0) {
			if (c == '>') {
				endOfData = true;
				return EOD;
			}
			if (!isWhiteSpace(c)) {
				if ((c0 = hexValue(c)) < 0)
					throw new IOException("invalid (c0): " + c);
				c = stream.getchar();
				if (c < 0)
					throw new IOException("premature end of input");
				if (c == '>') {
					endOfData = true;
					return c0 * 16;
				}
				if (!isWhiteSpace(c)) {
					if ((c1 = hexValue(c)) < 0)
						throw new IOException("invalid (c1): " + c);
					return c0 * 16 + c1;
				}
			}
		}
		return EOD;
	}

	public void encode(int c) throws IOException {
		if (charCounter >= 16) {
			stream.putchar('\n');
			charCounter = 0;
		}
		if (charCounter > 0) {
		    stream.putchar(' ');
		}
		stream.putchar(toHex((c >> 4) & 0x0f));
		stream.putchar(toHex(c & 0x0f));
		charCounter++;
	}

	static int toHex(int val) {
		if (0 <= val && val <= 9)
			return val + '0';
		if (10 <= val && val <= 15)
			return val - 10 + 'a';
		throw new IllegalArgumentException();
	}

	static int hexValue(int c) {
		int result;
		if (c >= '0' && c <= '9') {
			result = c - '0';
		} else if (c >= 'a' && c <= 'f') {
			result = c - 'a' + 10;
		} else if (c >= 'A' && c <= 'F') {
			result = c - 'A' + 10;
		} else {
			result = -1;
		}
		return result;
	}

}
