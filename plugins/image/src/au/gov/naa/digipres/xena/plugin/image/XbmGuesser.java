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
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for the XBM (X11 BitMap) image format
 * 
 */
//public class XbmGuesser extends DefaultGuesser {
public class XbmGuesser extends Guesser {
	//private static final byte[][] xbmMagic = {{'B', 'M'}};
	private static final String[] xbmExtensions = {"xbm"};
	private static final String[] xbmMime = {"image/xbm"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public XbmGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(XbmFileType.class);
		//FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(xbmExtensions, xbmMagic, xbmMime, type)};
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(xbmExtensions, new byte[0][0], xbmMime, type)};
		descriptorArr = tempFileDescriptors;
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(type);
		String mimeType = source.getMimeType();
		if (mimeType != null && (mimeType.equals("image/x-xbitmap") || mimeType.equals("image/x-xbm"))) {
			guess.setMimeMatch(true);
		}

		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();
		if (extension.equalsIgnoreCase("xbm")) {
			guess.setExtensionMatch(true);
		}

		// Check Magic Number/Data Match
		// TODO: Can this be changed? - XBM files do NOT have a magic number, so do not check for it, as may get false positives

		return guess;
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
	public String getName() {
		return "XBM Guesser";
	}

	@Override
	public Type getType() {
		return type;
	}

	/**
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

}
