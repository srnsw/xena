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

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser  for the Microsoft MSG  file format.
 *
 */
public class MsgGuesser extends Guesser {

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public MsgGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(MsgFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("msg")) {
			guess.setExtensionMatch(true);
		}
		POIFSFileSystem fs = null;
		try {
			fs = new POIFSFileSystem(source.getByteStream());
			guess.setMagicNumber(true);
		} catch (IOException x) {
			// an I/O error occurred, or the InputStream did not provide a compatible
			// POIFS data structure
			guess.setPossible(false);
		}
		return guess;
	}

	@Override
	public String getName() {
		return "MsgGuesser";
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
		return new FileTypeDescriptor[0];
	}

}
