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

package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.BinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Guesser for binary (non-character) files.
 *
 */
public class BinaryGuesser extends Guesser {

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public BinaryGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(BinaryFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		InputStream in = source.getByteStream();
		int c = -1;
		int total = 0;
		while (total < 65536 && 0 <= (c = in.read())) {
			total++;
			// i have a better idea.
			// lets use the xml character validator.
			if (!XMLCharacterValidator.isValidCharacter((char) c)) {
				guess.setDataMatch(GuessIndicator.TRUE);
				break;
			}

			// if (Character.isISOControl(c)) {
			// if (!(c == '\r' || c == '\n' || c == '\t' || c == '\f' || c == '\\')) {
			// guess.setDataMatch(GuessIndicator.TRUE);
			// System.out.println("Found control char! it is:" + c +
			// " in hex:" + Integer.toHexString(c) +
			// " and the char renders as: [" + (char)c + "]" +
			// " and it is at: " + total +
			// " and this char valid returns: " + XMLCharacterValidator.isValidCharacter((char)c)) ;
			//                    
			//                    
			//                    
			// break;
			// }
			// }

		}

		guess.setPriority(GuessPriority.LOW);
		return guess;
	}

	@Override
    public String getName() {
		return "BinaryGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess bestGuess = new Guess();
		bestGuess.setDataMatch(true);
		return bestGuess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
