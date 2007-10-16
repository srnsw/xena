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

public class PNGGuesser extends Guesser {

	static byte[] pngmagic = {new Integer(0x89).byteValue(), 'P', 'N', 'G'};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PNGGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PngFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource xis) throws XenaException, IOException {

		Guess guess = new Guess(type);

		// Extension and MIME
		String type = xis.getMimeType();
		String id = xis.getSystemId().toLowerCase();
		if (type.equals("image/png")) {
			guess.setMimeMatch(true);
		}
		if (id.endsWith(".png")) {
			guess.setExtensionMatch(true);
		}

		// Magic Number
		byte[] first = new byte[4];
		xis.getByteStream().read(first);
		boolean magicMatch = GuesserUtils.compareByteArrays(first, pngmagic);
		guess.setMagicNumber(magicMatch);

		// Need to find a good way to check for a data match, other than just trying to render it...
		// Or just using magic number match, as we do at the moment...
		guess.setDataMatch(magicMatch);

		return guess;
	}

	@Override
    public String getName() {
		return "PNGGuesser";
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
