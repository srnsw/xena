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

package au.gov.naa.digipres.xena.plugin.csv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Class to guess CSV files. 
 * CSV - Comma (Colon? Character?!?) seperated value file.
 * Fairly simple really... we look for our delimiters (comma, tab and colon)
 * and see if the data matches what we expect.
 * 
 * If it doesnt, then it probably isn't a CSV file.
 * 
 * If it does, check the extension and return based on that:
 *  GUESS_EXT_LIKELY if the extension matches,
 *  GUESS_DATA_LIKELY if not.
 *  
 *  We dont want to return a csv guess based solely on the filename coz, well,
 *  we could end up normalising all sorts of crazy files! 
 * 
 * 
 * AAK: Alright Chris. There are so many things wrong here I dont know where to begin.
 * Rest assured it has changed significantly.
 * 
 */
public class CsvGuesser extends Guesser {
	static final char[] sepChars = {',', '\t', ':', '|'};

	char guessedDelimiter = 0;

	private Type type;

	public CsvGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(CsvFileType.class);
	}

	public char getGuessedDelimiter() {
		return guessedDelimiter;
	}

	@Override
    public Type getType() {
		return type;
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		String line;
		int minCommas = Integer.MAX_VALUE;
		int maxCommas = 0;
		int numWithNoCommas = 0;
		int numLines = 0;
		int[] sepCount = new int[sepChars.length];

		// if the name is .csv or .tsv, nail it!
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull().toLowerCase();
		if (extension.equals("csv") || extension.equals("tsv")) {
			guess.setExtensionMatch(true);
		}

		// Imagine we have a huge binary file containing all nulls.
		// Without this limitation, the program will run out of memory...
		// TODO: aak: hmm. not super sure to what the preceding comment is referring. Investigation may be required.
		byte[] buf = new byte[1024 * 64];
		int sz = source.getByteStream().read(buf);
		ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, sz);
		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(bais));
		outer: while ((line = br.readLine()) != null) {
			numLines++;
			int count = 0;
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				for (int j = 0; j < sepChars.length; j++) {
					if (c == sepChars[j]) {
						count++;
						sepCount[j]++;
					}
				}
				if (Character.isISOControl(c)) {
					if (!(c == '\r' || c == '\n' || c == '\t')) {
						guess.setPossible(false);
						break outer;
					}
				}

			}
			if (count < minCommas) {
				minCommas = count;
			}
			if (maxCommas < count) {
				maxCommas = count;
			}
			if (count == 0) {
				numWithNoCommas++;
			}
		}

		if (numWithNoCommas == 0 && 2 < numLines) {
			guess.setDataMatch(true);
		}

		int max = 0;
		for (int i = 0; i < sepChars.length; i++) {
			if (max < sepCount[i]) {
				max = sepCount[i];
				guessedDelimiter = sepChars[i];
			}
		}

		// heheheh - lets over ride the previous csv guesser! burn!
		guess.setPriority(GuessPriority.HIGH);

		return guess;
	}

	int max(int i1, int i2) {
		if (i1 < i2) {
			return i2;
		}
		return i1;
	}

	@Override
    public String getName() {
		return "CSVGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		guess.setPriority(GuessPriority.HIGH);
		return guess;
	}

}
