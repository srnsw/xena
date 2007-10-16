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

package au.gov.naa.digipres.xena.plugin.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for MBOX email files.
 *
 */
public class MboxGuesser extends Guesser {

	static final String FROM_TEXT = "From ";
	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public MboxGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(MboxDirFileType.class);
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);

		if (source instanceof MultiInputSource) {
			guess.setPossible(true);
		}

		InputStream is = source.getByteStream();
		Reader reader = new InputStreamReader(is);
		char[] from = new char[FROM_TEXT.length() + 1];
		reader.read(from);
		String froms = new String(from);
		if (froms.substring(0, FROM_TEXT.length()).equalsIgnoreCase(FROM_TEXT) || froms.substring(1).equalsIgnoreCase(FROM_TEXT)) {
			guess.setDataMatch(true);
			guess.setPriority(GuessPriority.HIGH);
		}
		return guess;
	}

	@Override
    public String getName() {
		return "MBoxGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setPossible(true);
		guess.setDataMatch(true);
		guess.setPriority(GuessPriority.HIGH);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

}
