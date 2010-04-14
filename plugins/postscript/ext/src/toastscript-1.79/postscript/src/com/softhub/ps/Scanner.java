
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

import java.io.IOException;

public class Scanner implements Stoppable {

	public static final int EOF = 0;
	public static final int NUMBER = 1;
	public static final int IDENT = 2;
	public static final int LITERAL = 3;
	public static final int IMMEDIATE = 4;
	public static final int STRING = 5;
	public static final int PROC_BEGIN = 6;
	public static final int PROC_END = 7;

	/**
	 * String parenthesis balance counter.
	 */
	private int stringbalance;

	/**
	 * Proc curly brace balance counter.
	 */
	private int procbalance;

	/**
	 * The char buffer.
	 */
	private StringBuffer charbuffer = new StringBuffer();

	/**
	 * The numeric value.
	 */
	private Number number;

	public Scanner() {
	}

	public String getString() {
		return new String(charbuffer);
	}

	public Number getNumber() {
		return number;
	}

	public int token(CharSequenceType cs) {
		int result = EOF;
		charbuffer.setLength(0);
		try {
			result = readToken(cs);
		} catch (IOException ex) {
			throw new Stop(IOERROR, "token " + cs + " " + ex);
		}
		return result;
	}

	public boolean defered() {
		return procbalance > 0;
	}

	private int readToken(CharSequenceType cs) throws IOException {
		while (true) {
			int c = cs.getchar();
			switch (c) {
			case -1:
				return EOF;
			case ')':
				throw new Stop(SYNTAXERROR, "readToken");
			case '%':
				readComment(cs);
				break;
			case '\0':
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case '\b':
			case '\t':
			case '\n':
			case 11:
			case '\f':
			case '\r':
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
			case ' ':
				break;
			case '(':
				readString(cs);
				return STRING;
			case '<':
				return readDictBeginOrHexString(cs);
			case '>':
				if ((c = cs.getchar()) != '>')
					throw new Stop(SYNTAXERROR, "readToken >");
				charbuffer.append('>');
				charbuffer.append('>');
				return IDENT;
			case '/':
				return readLiteralName(cs);
			case '{':
				procbalance++;
				return PROC_BEGIN;
			case '}':
				if (procbalance <= 0)
					throw new Stop(SYNTAXERROR, "readToken }");
				procbalance--;
				return PROC_END;
			case '[':
			case ']':
				charbuffer.append((char) c);
				return IDENT;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '+':
			case '-':
			case '.':
				cs.ungetchar(c);
				readName(cs);
				return convertNameToNumber(cs);
			default:
				cs.ungetchar(c);
				readName(cs);
				return IDENT;
			}
		}
	}

	/**
	 * Read over comments.
	 */
	private void readComment(CharSequenceType cs) throws IOException {
		int c = cs.getchar();
		if (c == '%') {
			readDSC(cs);
		} else {
			// skip
			while (c >= 0 && c != '\n' && c != '\r') {
				c = cs.getchar();
			}
		}
	}

	/**
	 * Read DSC.
	 */
	private void readDSC(CharSequenceType cs) throws IOException {
		int c;
		StringBuffer buffer = new StringBuffer();
	    while ((c = cs.getchar()) >= 0 && c != '\n' && c != '\r') {
			buffer.append((char) c);
	    }
		String dsc = buffer.toString();
		if (dsc.equals("EOF")) {
			cs.close();
		}
	}

	private int readLiteralName(CharSequenceType cs) throws IOException {
		int c;
		if ((c = cs.getchar()) == '/') {
			readName(cs);
			return IMMEDIATE;
		} else {
			cs.ungetchar(c);
			readName(cs);
			return LITERAL;
		}
	}

	private void readName(CharSequenceType cs) throws IOException {
		while (true) {
			int c = cs.getchar();
			switch (c) {
			case ')':
				throw new Stop(SYNTAXERROR, "readName");
			case -1:
			case '%':
			case '<':
			case '>':
			case '(':
			case '{':
			case '}':
			case '[':
			case ']':
			case '/':
				cs.ungetchar(c);
				// fall through
			case ' ':
			case '\t':
			case '\r':
			case '\n':
			case '\b':
			case '\f':
			case '\0':
				return;
			default:
				charbuffer.append((char) c);
				break;
			}
		}
	}

	private int readDictBeginOrHexString(CharSequenceType cs) throws IOException {
		int result, c = cs.getchar();
		if (c == '<') {
			charbuffer.append('<');
			charbuffer.append('<');
			result = IDENT;
		} else {
			cs.ungetchar(c);
			readHexString(cs);
			result = STRING;
		}
		return result;
	}

	private int convertNameToNumber(CharSequenceType cs) {
		try {
			try {
			    number = Integer.valueOf(new String(charbuffer));
			} catch (NumberFormatException ex) {
			    number = Double.valueOf(new String(charbuffer));
			}
			return NUMBER;
		} catch (NumberFormatException ex) {
			return convertNameToRadix();
		}
	}

	private int convertNameToRadix() {
		int i, c, radix = 0;
		int len = charbuffer.length();
		char buffer[] = new char[len];
		charbuffer.getChars(0, len, buffer, 0);
		// scan the base
		for (i = 0; radix < 26 && i < len; i++) {
			c = buffer[i];
			if (i > 0 && c == '#')
				break;
			if (c < '0' || c > '9')
				return IDENT;
			radix = radix * 10 + (c - '0');
		}
		if (radix > 26)
			return IDENT;
		if (i >= len)
			return IDENT;
		// parse the NumberType using base
		String s = new String(buffer, i+1, len-i-1);
		try {
			number = Integer.valueOf(s, radix);
			return NUMBER;
		} catch (NumberFormatException ex) {
			return IDENT;
		}
	}

	private void readString(CharSequenceType cs) throws IOException {
		int c = -1;
		stringbalance = 1;
		charbuffer.setLength(0);
		while (stringbalance > 0 && (c = cs.getchar()) >= 0) {
			switch (c) {
			case '\\':
				readEscapeChar(cs);
				break;
			case '(':
				stringbalance++;
				charbuffer.append((char) c);
				break;
			case ')':
				if (--stringbalance > 0)
					charbuffer.append((char) c);
				break;
			default:
				charbuffer.append((char) c);
				break;
			}
		}
		if (c < 0)
			throw new Stop(SYNTAXERROR, "readString");
	}

	private void readEscapeChar(CharSequenceType cs) throws IOException {
		int c = cs.getchar();
		if (c < 0)
			throw new Stop(SYNTAXERROR, "readEscapeChar");
		switch (c) {
		case 'n':
			charbuffer.append('\n');
			break;
		case 'r':
			charbuffer.append('\r');
			break;
		case 't':
			charbuffer.append('\t');
			break;
		case 'f':
			charbuffer.append('\f');
			break;
		case 'b':
			charbuffer.append('\b');
			break;
		case '\\':
			charbuffer.append('\\');
			break;
		case '(':
		case ')':
			charbuffer.append((char) c);
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
			readOctalChar(cs, hexValue(c));
			break;
		default:
			charbuffer.append((char) c);
		}
	}

	private void readOctalChar(CharSequenceType cs, int c0) throws IOException {
		int c, c1, c2;
		c = cs.getchar();
		if (c < 0)
			throw new Stop(SYNTAXERROR, "readOctalChar");
		if ((c1 = hexValue(c)) < 0) {
			charbuffer.append((char) c0);
			cs.ungetchar(c);
			return;
		}
		c = cs.getchar();
		if (c < 0)
			throw new Stop(SYNTAXERROR, "readOctalChar2");
		if ((c2 = hexValue(c)) < 0) {
			charbuffer.append((char) (c0 * 8 + c1));
			cs.ungetchar(c);
			return;
		}
		charbuffer.append((char) (c0 * 64 + c1 * 8 + c2));
	}

	private void readHexString(CharSequenceType cs) throws IOException {
		int c = -1, c0, c1;
		boolean eos = false;
		charbuffer.setLength(0);
		while (!eos && (c = cs.getchar()) >= 0 && c != '>') {
			if ((c0 = hexValue(c)) >= 0) {
				c = cs.getchar();
				if (c < 0)
					throw new Stop(SYNTAXERROR, "readHexString");
				if ((c1 = hexValue(c)) >= 0) {
					charbuffer.append((char) (c0 * 16 + c1));
				} else if (c == '>') {
					charbuffer.append((char) (c0 * 16));
					eos = true;
				}
			}
		}
		if (c < 0)
			throw new Stop(SYNTAXERROR, "readHexString < 0");
	}

	public static int hexValue(int c) {
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
