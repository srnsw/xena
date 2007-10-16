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
 * Created on 27/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessIndicator;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.Type;

public abstract class OfficeGuesser extends Guesser {
	protected static byte[][] officeMagic = {{(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0, (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1}};

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	protected abstract FileTypeDescriptor[] getFileTypeDescriptors();

	protected abstract String getOfficeTypeString();

	protected Guess guess(XenaInputSource xis, Type fileType) throws XenaException, IOException {

		Guess guess = new Guess(fileType);
		FileTypeDescriptor[] descriptorArr = getFileTypeDescriptors();

		// get the mime type...
		String type = xis.getMimeType();
		if (type != null && !type.equals("")) {
			for (int i = 0; i < descriptorArr.length; i++) {
				if (descriptorArr[i].mimeTypeMatch(type)) {
					guess.setMimeMatch(true);
					break;
				}
			}
		}

		// MAGIC NUMBER
		// -> do not set to false because it could be a word processor format
		// that the normaliser can handle but we dont know the magic number for it...

		byte[] first = new byte[10];
		xis.getByteStream().read(first);

		for (int i = 0; i < descriptorArr.length; i++) {
			if (descriptorArr[i].magicNumberMatch(first)) {
				guess.setMagicNumber(true);
				break;
			}
		}

		// extension...
		// Get the extension...
		FileName name = new FileName(xis.getSystemId());
		String extension = name.extenstionNotNull();

		boolean extMatch = false;
		if (!extension.equals("")) {
			for (int i = 0; i < descriptorArr.length; i++) {
				if (descriptorArr[i].extensionMatch(extension)) {
					extMatch = true;
					break;
				}
			}
		}
		guess.setExtensionMatch(extMatch);

		// Data match
		try {
			guess.setDataMatch(officeTypeMatched(xis));
		} catch (IOException ex) {
			// Not a POIFS, but could still be a non-POIFS word processor
			// file of some sort, so do nothing
		}

		// If the office file does not have an extension, it will be given
		// the same rank by all the office guessers. So we'll rank them
		// in order of probability (ie docs are more common than xls etc)
		// using priority.
		// TODO: A better way of determining office file type needs to be found!

		return guess;
	}

	/**
	 * Returns an indication of whether this particular office type matches
	 * the "CompObj" application name in the file header. Subclass implementations
	 * of this abstract class (ie for Word, Spreadsheet, Presentation etc) implement
	 * the getOfficeTypeString method which is the string used to check the type.
	 * 
	 * @param xis
	 * @return Indication of a type match, using the GuessIndicator fields UKNOWN, FALSE and TRUE
	 * @throws IOException
	 */
	public GuessIndicator officeTypeMatched(XenaInputSource xis) throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(xis.getByteStream());
		DirectoryEntry root = fs.getRoot();

		//
		// Retrieve the CompObj data and validate the file format
		//
		OfficeCompObj compObj = new OfficeCompObj(new DocumentInputStream((DocumentEntry) root.getEntry("\1CompObj")));

		GuessIndicator matchIndicator = GuessIndicator.UNKNOWN;

		String appName = compObj.getApplicationName();

		if (appName != null && !appName.equals("")) {
			if (appName.indexOf(getOfficeTypeString()) >= 0) {
				matchIndicator = GuessIndicator.TRUE;
			} else {
				matchIndicator = GuessIndicator.FALSE;
			}
		}

		return matchIndicator;
	}
}
