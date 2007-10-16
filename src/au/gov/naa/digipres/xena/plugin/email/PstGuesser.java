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

package au.gov.naa.digipres.xena.plugin.email;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess the Microsoft PST email  file format.
 *
 */
public class PstGuesser extends Guesser {
	static byte[] pstmagic = {'!', 'B', 'D', 'N'};
	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PstGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PstFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("pst")) {
			guess.setExtensionMatch(true);
		}

		byte[] first = new byte[4];
		source.getByteStream().read(first);
		if (compareMagic(first, pstmagic)) {
			guess.setMagicNumber(true);
		} else {
			guess.setPossible(false);
		}

		return guess;

	}

	static boolean compareMagic(byte[] b1, byte[] b2) {
		for (int i = 0; i < b2.length && i < b1.length; i++) {
			if (b2[i] != b1[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
    public String getName() {
		return "PstGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
