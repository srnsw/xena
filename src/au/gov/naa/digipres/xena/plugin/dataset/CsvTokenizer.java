package au.gov.naa.digipres.xena.plugin.dataset;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to do low level CSV file parsing. We don't use StreamTokenizer, as
 * tempting as it may be, because the code in
 * there is just too complex and I don't trust it. It does special things with
 * backslashes, numerals, dots and all sorts of stuff, that is not well
 * documented and hard to predict. Even if you did use it, dealing with all the
 * special cases just makes it more complicated than this code which just does
 * what we want.
 *
 * @author     Chris Bitmead
 * @created    25 November 2002
 */
class CsvTokenizer {
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

	protected List header;

	protected boolean useQuoteCharacter = false;

	protected List putBack;

	public void putBack(List record) {
		assert putBack == null;
		putBack = record;
	}

	public CsvTokenizer(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
	}

	public CsvTokenizer(BufferedReader br) {
		this.br = br;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		CharArrayWriter caw = new CharArrayWriter();
		caw.write("x,y,\\z\n");
		caw.write("a,\\\"b,c\n");
		caw.write("a,\"b,c\n");
		caw.write("0,.,.01,0.1");
		CharArrayReader car = new CharArrayReader(caw.toCharArray());
		BufferedReader br = new BufferedReader(car);
		CsvTokenizer t = new CsvTokenizer(br);

		List record;
		while ((record = t.nextRecord()) != null) {
			Iterator it2 = record.iterator();
			while (it2.hasNext()) {
				String fld = (String)it2.next();
				System.out.print(fld + ":");
			}
			System.out.println("#");
		}
	}

	public void setFirstRowFieldNames(boolean v) throws IOException {
		if (v && header == null) {
			header = nextRecord();
		}
	}

	public void setFieldDelimiter(char fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	public void setQuoteCharacter(char quoteCharacter) {
		this.quoteCharacter = quoteCharacter;
	}

	public void setUseQuoteCharacter(boolean useQuoteCharacter) {
		this.useQuoteCharacter = useQuoteCharacter;
	}

	public char getFieldDelimiter() {
		return fieldDelimiter;
	}

	public char getQuoteCharacter() {
		return quoteCharacter;
	}

	public boolean isUseQuoteCharacter() {
		return useQuoteCharacter;
	}

	public List getHeader() {
		return header;
	}

	public List nextRecord() throws IOException {
		if (putBack != null) {
			List rtn = putBack;
			putBack = null;
			return rtn;
		}
		boolean isInQuote = false;
		String field = "";
		List record = new ArrayList();
		int currentRecordLength = 0;
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
					char[] chars = {
						(char)fieldDelimiter};
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
			if (c == fieldDelimiter || (useQuoteCharacter && c == quoteCharacter) || c == '\n') {
				if (nextToken == null) {
					if (c == fieldDelimiter) {
						return DELIM;
					} else if (useQuoteCharacter && c == quoteCharacter) {
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
			nextToken += (char)c;
		}
		if (nextToken == null) {
			return EOF;
		} else {
			return WORD;
		}
	}

	protected String addField(String field, String add) {
		if (add == null) {
			int i = 10;
		}
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
