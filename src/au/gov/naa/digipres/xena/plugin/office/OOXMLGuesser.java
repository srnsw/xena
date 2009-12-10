/**
 * This file is part of office.
 * 
 * office is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * office is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with office; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessIndicator;

/**
 * @author Justin Waddell
 *
 */
public abstract class OOXMLGuesser extends DefaultGuesser {

	public static final byte[][] ZIP_MAGIC = {{0x50, 0x4B, 0x03, 0x04}};

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser#guess(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public Guess guess(XenaInputSource source) throws IOException {

		// Use the superclass to get a 'default' guess just based on extension, magic number and mime type.
		Guess guess = super.guess(source);

		// If this is a zip file, look for a certain filename within the zip
		if (guess.getMagicNumber() == GuessIndicator.TRUE) {
			ZipInputStream zipInput = new ZipInputStream(source.getByteStream());

			ZipEntry zipEntry = zipInput.getNextEntry();
			boolean found = false;
			while (zipEntry != null) {
				if (zipEntry.getName().equals(getIdentifyingFilename())) {
					found = true;
					break;
				}
				zipEntry = zipInput.getNextEntry();
			}

			guess.setDataMatch(found);

		}

		return guess;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser#createBestPossibleGuess()
	 */
	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = super.createBestPossibleGuess();

		// The OOXML guesser can set 'data match' to true.
		guess.setDataMatch(true);

		return guess;
	}

	protected abstract String getIdentifyingFilename();

}
