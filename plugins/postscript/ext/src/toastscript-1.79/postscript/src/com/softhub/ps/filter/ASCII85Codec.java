
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

public class ASCII85Codec extends AbstractCodec {

	private final static int DECODING[] = { 52200625, 614125, 7225, 85, 1 };

	private byte buffer[] = new byte[4];
	private int count;

	public void close() throws IOException {
		if (mode == CharStream.WRITE_MODE) {
			if (count > 0) {
				while (count < 4) {
					buffer[count++] = 0;
				}
				flushEncodingBuffer();
			}
			stream.putchar('~');
			stream.putchar('>');
		}
	}

	public int decode() throws IOException {
		if (endOfData && count <= 0)
			return EOD;
		if (count <= 0) {
			fillDecodingBuffer();
			if (count <= 0)
				return EOD;
		}
		return buffer[--count];
	}

	public void encode(int c) throws IOException {
		if (count >= 3) {
		    buffer[count] = (byte) c;
			flushEncodingBuffer();
			count = 0;
		} else {
		    buffer[count++] = (byte) c;
		}
	}

	private void fillDecodingBuffer() throws IOException {
		int c, i = 0;
		long m = 0;
		while (!endOfData && i < buffer.length+1 && (c = stream.getchar()) >= 0) {
			if (!isWhiteSpace(c)) {
				if (c == 'z') {
					if ((i % 5) != 0)
						throw new IOException("invalid z position");
					i += 5;
					m = 0;
				} else if ('!' <= c && c <= 'u') {
					m += (c - '!') * DECODING[i++];
				} else if (c == '~') {
					if ((c = stream.getchar()) != '>')
						throw new IOException("bad end of decoding character: " + c);
					endOfData = true;
				} else {
					throw new IOException("invalid character: " + c);
				}
				if ((i % 5) == 0 || endOfData) {
					if (m >= (1L << 32))
						throw new IOException("out of range: " + m);
					buffer[count++] = (byte) (m & 255L);
					buffer[count++] = (byte) ((m >> 8) & 255L);
					buffer[count++] = (byte) ((m >> 16) & 255L);
					buffer[count++] = (byte) ((m >> 24) & 255L);
					m = 0;
				}
			}
		}
		if (!endOfData) {
			// look ahead for end of filter sequence
			if ((c = stream.getchar()) == '~') {
				if ((c = stream.getchar()) != '>')
					throw new IOException("bad end of decoding: " + c);
				endOfData = true;
			} else {
				stream.ungetchar(c);
			}
		}
	}

	private void flushEncodingBuffer() throws IOException {
		long val = bufferToLong();
		if (val == 0) {
			stream.putchar('z');
		} else {
			for (int i = 0; i < 5; i++) {
				int m = (int) (val / DECODING[i]);
				stream.putchar((m % 85) + '!');
			}
		}
	}

	private long bufferToLong() {
		long result = 0;
		for (int i = 0; i < 4; i++) {
			result <<= 8;
			result += buffer[i];
		}
		return result;
	}

}
