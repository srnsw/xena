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

package au.gov.naa.digipres.xena.plugin.xml;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess a file of a XML of a random type.
 *
 */
public class XmlGuesser extends Guesser {

	// Read in a maximum of 64k characters when checking for XML tag
	private static final int MAX_CHARS_READ = 64 * 1024;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public XmlGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(XmlFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(type);

		// Check extension
		if (source.getSystemId().toLowerCase().endsWith(".xml")) {
			guess.setExtensionMatch(true);
		}

		// Check Magic Number/Data Match
		Reader isReader = source.getCharacterStream();
		char[] charArr = new char[MAX_CHARS_READ];
		isReader.read(charArr, 0, MAX_CHARS_READ);

		BufferedReader rd = new BufferedReader(new CharArrayReader(charArr));
		String line = rd.readLine();

		// Get the first non-blank line.
		while (line != null) {
			line = line.trim();
			if (line.equals("")) {
				line = rd.readLine();
			} else {
				// If the first characters are "<?xml" then we have matched magic number.
				// Do not set to false if there is no match, as the xml declaration is optional.
				if (line.toLowerCase().startsWith("<?xml")) {
					guess.setMagicNumber(true);
				}

				// If the first character is a "<" then we have a data match.
				// If the first character is not a "<" then this is not an XML file.
				guess.setDataMatch(line.startsWith("<"));
				break;
			}
		}

		return guess;
	}

	@Override
	public String getName() {
		return "XMLGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return new FileTypeDescriptor[0];
	}
}
