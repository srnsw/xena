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

package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for JAI supported image types other than the core JPEG and PNG
 * 
 */
public class ImageGuesser extends Guesser {
	static byte[] gifmagic = {'G', 'I', 'F'};

	static byte[] gifTail = {0x00, 0x3b};

	static byte[] bmpmagic = {'B', 'M'};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public ImageGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(ImageFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		String type = source.getMimeType();

		// get the mime type...
		if (type != null && (type.equals("image/gif") || type.equals("image/bmp"))) {
			guess.setMimeMatch(true);
		}

		// Get the extension...
		String id = source.getSystemId().toLowerCase();
		if (id.endsWith(".gif") || id.endsWith(".bmp")) {
			guess.setExtensionMatch(true);
		}

		// Get the magic number
		byte[] first = new byte[3];
		source.getByteStream().read(first);
		if (GuesserUtils.compareByteArrays(first, gifmagic) || GuesserUtils.compareByteArrays(first, bmpmagic)) {
			guess.setMagicNumber(true);

			// TODO: A better way of checking for data match
			guess.setDataMatch(true);

		} else {
			guess.setMagicNumber(false);
			guess.setPossible(false);
		}

		return guess;
	}

	@Override
    public String getName() {
		return "ImageGuesser";
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
