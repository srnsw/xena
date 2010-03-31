/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.email.trim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to parse the TRIM attachment header line in a TRIM email file.
 * Name/Value pairs are separated by commas, but as unescaped commas can
 * exist in values we can't just split on commas, we have to use this class
 * to see if we are inside a quote set or not.
 * 
 * created 28/02/2006
 * email
 * Short desc of class:
 */
class TrimAttachmentTokenizer {
	protected final static int EOF = -1;

	protected final static int WORD = -2;

	protected final static int DELIM = -3;

	protected final static int QUOTE = -4;

	protected final static int EOL = -5;

	protected String nextToken;

	protected char quoteCharacter = '"';

	protected char fieldDelimiter = ',';

	protected BufferedReader br;

	protected String line;

	protected int linePos;

	protected List<String> putBack;

	public void putBack(List<String> record) {
		assert putBack == null;
		putBack = record;
	}

	public TrimAttachmentTokenizer(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
	}

	public TrimAttachmentTokenizer(BufferedReader br) {
		this.br = br;
	}

	public TrimAttachmentTokenizer(String str) {
		br = new BufferedReader(new StringReader(str));
	}

	public List<String> nextRecord() throws IOException {
		if (putBack != null) {
			List<String> rtn = putBack;
			putBack = null;
			return rtn;
		}
		boolean isInQuote = false;
		String field = "";
		List<String> record = new ArrayList<String>();
		int ttype;
		boolean foundStuff = false;
		while ((ttype = getNextToken()) != EOF) {
			foundStuff = true;
			switch (ttype) {
			case EOL:
				if (isInQuote) {
					field = addField(field, "\n");
				} else {
					record.add(field);
					return record;
				}
				break;
			case WORD:
				field = addField(field, nextToken);
				break;
			case DELIM:
				if (isInQuote) {
					char[] chars = {fieldDelimiter};
					field = addField(field, new String(chars));
				} else {
					record.add(field);
					field = "";
				}
				break;
			case QUOTE:
				if (isInQuote) {
					isInQuote = false;
				} else {
					isInQuote = true;
				}
				break;
			default:
				throw new IOException("default: Can't Happen");
			}
		}
		if (foundStuff) {
			record.add(field);
		}
		if (0 < record.size()) {
			return record;
		}
		return null;
	}

	protected int getNextToken() throws IOException {
		int c;
		nextToken = null;
		while (0 <= (c = read())) {
			if (c == fieldDelimiter || c == quoteCharacter || c == '\n') {
				if (nextToken == null) {
					if (c == fieldDelimiter) {
						return DELIM;
					} else if (c == quoteCharacter) {
						return QUOTE;
					} else if (c == '\n') {
						return EOL;
					} else {
						throw new IOException("Can't Happen");
					}
				} else {
					putBack();
					return WORD;
				}
			}
			if (nextToken == null) {
				nextToken = "";
			}
			nextToken += (char) c;
		}
		if (nextToken == null) {
			return EOF;
		} else {
			return WORD;
		}
	}

	protected String addField(String field, String add) {
		if (field == null) {
			return add;
		} else {
			return field + add;
		}
	}

	protected int read() throws IOException {
		if (line == null || line.length() < linePos) {
			line = br.readLine();
			linePos = 0;
		}
		int rtn = EOF;
		if (line != null) {
			if (linePos == line.length()) {
				rtn = '\n';
				linePos++;
			} else {
				rtn = line.charAt(linePos++);
			}
		}
		return rtn;
	}

	protected void putBack() throws IOException {
		if (linePos <= 0) {
			throw new IOException("Can't pushback twice");
		}
		linePos--;
	}
}
