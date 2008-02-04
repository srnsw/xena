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

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser to guess the Microsoft PST email  file format.
 *
 */
public class PstGuesser extends DefaultGuesser {
	private Type type;

	private static final byte[][] pstMagic = {{'!', 'B', 'D', 'N'}};
	private static final String[] pstExtensions = {"pst"};
	private static final String[] pstMime = {};

	private FileTypeDescriptor[] descriptorArr = {new FileTypeDescriptor(pstExtensions, pstMagic, pstMime)};

	/**
	 * @throws XenaException 
	 * 
	 */
	public PstGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(PstFileType.class);
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

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

}
