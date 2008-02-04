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
 */

package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * @author Justin Waddell
 *
 */
public abstract class DefaultGuesser extends Guesser {

	/**
	 * @param guesserManager
	 */
	public DefaultGuesser(GuesserManager guesserManager) {
		super(guesserManager);
	}

	/**
	 * 
	 */
	public DefaultGuesser() {
		super();
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(getType());
		String mimeType = source.getMimeType();

		// get the mime type...
		if (mimeType != null && !mimeType.equals("")) {
			for (FileTypeDescriptor element : getFileTypeDescriptors()) {
				if (element.mimeTypeMatch(mimeType)) {
					guess.setMimeMatch(true);
					break;
				}
			}
		}

		// Get the extension...
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();

		boolean extMatch = false;
		if (!extension.equals("")) {
			for (FileTypeDescriptor element : getFileTypeDescriptors()) {
				if (element.extensionMatch(extension)) {
					extMatch = true;
					break;
				}
			}
		}
		guess.setExtensionMatch(extMatch);

		// Get the magic number.
		byte[] first = new byte[4];
		source.getByteStream().read(first);
		boolean magicMatch = false;

		for (FileTypeDescriptor element : getFileTypeDescriptors()) {
			if (element.magicNumberMatch(first)) {
				magicMatch = true;
				break;
			}
		}
		guess.setMagicNumber(magicMatch);

		return guess;
	}

}
