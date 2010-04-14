
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

public class EExecCodecFactory {

	private final static int ESC = 128;

	private final static int TXT = 1;
	private final static int BIN = 2;
	private final static int END = 3;
	private final static int HEX = 4;

	public static Codec createCodec(CharStream stream, int mode)
		throws IOException
	{
		Codec codec;
		int esc = stream.getchar();
		if (esc < 0)
			throw new IOException();
		if (esc == ESC) {
			int enc = stream.getchar();
			if (enc < 0)
				throw new IOException();
			switch (enc) {
			case TXT:
				codec = new EExecTextCodec();
				codec.open(stream, mode);
				skip(stream, 4);
				break;
			case BIN:
				codec = new EExecBinaryCodec();
				codec.open(stream, mode);
				skip(stream, 4);
				skipDecode(codec, 4);
				break;
			default:
				throw new IOException();
			}
		} else if (esc == '%') {
			stream.ungetchar(esc);
			codec = new EExecNullCodec();
			codec.open(stream, mode);
		} else {
			stream.ungetchar(esc);
			codec = new EExecHexCodec();
			codec.open(stream, mode);
			skipDecode(codec, 4);
		}
		return codec;
	}

	private static void skip(CharStream stream, int n) throws IOException {
		for (int i = 0; i < n; i++) {
			stream.getchar();
		}
	}

	private static void skipDecode(Codec codec, int n) throws IOException {
		for (int i = 0; i < n; i++) {
			codec.decode();
		}
	}

	static class EExecTextCodec extends EExecNullCodec implements Codec {

		public int decode() throws IOException {
			int c = stream.getchar();
			if (c == ESC) {
				c = stream.getchar();
				if (c == END) {
					c = -1;
				}
			}
			return c;
		}

	}

	static class EExecNullCodec extends AbstractCodec {

		public int decode(CharStream cs) throws IOException {
			return cs.getchar();
		}

		public int decode(int c) throws IOException {
			return c;
		}

		public int decode() throws IOException {
			return stream.getchar();
		}

	}

	static class EExecBinaryCodec extends BinaryCodec {

		EExecBinaryCodec() {
			super(EEXEC_SEED);
		}

	}

	static class EExecHexCodec extends BinaryCodec {

		EExecHexCodec() {
			super(EEXEC_SEED);
		}

		public int decode() throws IOException {
			return decode(nextHex(stream));
		}

		private static int nextHex(CharStream cs) throws IOException {
			int c0 = 0, c1 = 0;
			c0 = nextNonBlankChar(cs);
			if (c0 < 0)
				return -1;
			c1 = nextNonBlankChar(cs);
			int x0 = ASCIIHexCodec.hexValue(c0);
			if (c1 < 0)
				return x0;
			int x1 = ASCIIHexCodec.hexValue(c1);
			return (x0 << 4) | x1;
		}

		private static int nextNonBlankChar(CharStream cs) throws IOException {
			int c = 0;
			do {
				c = cs.getchar();
			} while (c >= 0 && c <= ' ');
			return c;
		}

	}

}
