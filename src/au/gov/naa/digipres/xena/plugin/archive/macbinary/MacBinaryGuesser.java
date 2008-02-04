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
 * Created on 15/05/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.macbinary;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class MacBinaryGuesser extends Guesser {
	private static final String[] binExtensions = {"bin"};
	private static final String[] binMime = {"application/macbinary"};
	private static final byte[][] binMagic = {{}};

	private FileTypeDescriptor[] binFileDescriptors = {new FileTypeDescriptor(binExtensions, binMagic, binMime),};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public MacBinaryGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(MacBinaryFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(getType());
		String mimeType = source.getMimeType();

		// get the mime type...
		if (mimeType != null && !mimeType.equals("")) {
			for (FileTypeDescriptor element : binFileDescriptors) {
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
			for (FileTypeDescriptor element : binFileDescriptors) {
				if (element.extensionMatch(extension)) {
					extMatch = true;
					break;
				}
			}
		}
		guess.setExtensionMatch(extMatch);

		// MacBinary has a weak magic number (first byte is 0) so while we won't be able to definitively confirm
		// that this file is a MacBinary file, we can confirm that it is NOT a MacBinary file by checking certain bytes
		// in the header
		byte[] header = new byte[128];
		source.getByteStream().read(header);
		if (header[0] != 0 || header[74] != 0 || header[82] != 0) {
			guess.setMagicNumber(false);
		}

		return guess;
	}

	@Override
	public String getName() {
		return "MacBinary Guesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
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
		return binFileDescriptors;
	}
}
