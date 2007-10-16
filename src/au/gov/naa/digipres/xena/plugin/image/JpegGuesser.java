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

/*
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class JpegGuesser extends Guesser {

	private static byte[] jpegmagic = {new Integer(-1).byteValue(), new Integer(-40).byteValue(), new Integer(-1).byteValue()};

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
	}

	@Override
    public Guess guess(XenaInputSource source) throws XenaException, IOException {
		// Guess guess = new Guess((FileType)TypeManager.singleton().lookup(JpegFileType.class));

		Guess guess = new Guess(new JpegFileType());

		String type = source.getMimeType();
		byte[] first = new byte[4];
		source.getByteStream().read(first);
		String id = source.getSystemId().toLowerCase();

		if (type.equals("image/jpeg")) {
			guess.setMimeMatch(true);
		}
		if (id.endsWith(".jpg") || id.endsWith(".jpeg")) {
			guess.setExtensionMatch(true);
		}
		if (GuesserUtils.compareByteArrays(first, jpegmagic)) {
			guess.setMagicNumber(true);

			// TODO: A better way of checking for Data Match
			guess.setDataMatch(true);
		} else {
			guess.setMagicNumber(false);
			guess.setPossible(false);
		}

		return guess;
	}

	@Override
    public String getName() {
		return "JpegGuesser";
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
		return new JpegFileType();
	}

}
