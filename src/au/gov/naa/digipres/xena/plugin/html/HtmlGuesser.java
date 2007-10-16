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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for HTML files.
 *
 */
public class HtmlGuesser extends Guesser {

	// Read in a maximum of 64k characters when checking for HTML tag
	private static final int MAX_CHARS_READ = 64 * 1024;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public HtmlGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(HtmlFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		String type = source.getMimeType();
		if (type != null && type.equals("text/html")) {
			guess.setMimeMatch(true);
		}

		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("htm")) {
			guess.setExtensionMatch(true);
		}

		// Check Magic Number/Data Match
		Reader isReader = source.getCharacterStream();
		char[] charArr = new char[MAX_CHARS_READ];
		isReader.read(charArr, 0, MAX_CHARS_READ);

		BufferedReader rd = new BufferedReader(new CharArrayReader(charArr));
		String line = rd.readLine();

		// HTML files are very flexible, and could have a lot of data
		// before the opening html tag (and may not have the html tag
		// at all... not much we can do about that though). So check
		// the first 100 lines for "<html".
		int count = 0;
		while (line != null && count < 100) {
			if (line.toLowerCase().indexOf("<html") >= 0 || line.toUpperCase().indexOf("<!DOCTYPE HTML") >= 0) {
				guess.setDataMatch(true);

				// If match is on first non-blank line, then we pretty much
				// have an HTML magic number...
				if (count == 0) {
					guess.setMagicNumber(true);
				}
				break;
			}
			if (!line.trim().equals("")) {
				count++;
			}
			line = rd.readLine();
		}

		return guess;
	}

	@Override
    public String getName() {
		return "HTMLGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setDataMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
