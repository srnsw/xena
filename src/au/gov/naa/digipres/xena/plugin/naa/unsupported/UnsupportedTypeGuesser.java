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
 * Created on 08/05/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Unsupported types are the file types that we know we will eventually want to handle but for which we currently
 * do not have a normaliser. Guessing and setting a unique type for these files will make it much easier to renormalise
 * these files at a later date.
 * 
 * created 24/05/2007
 * naa
 * Short desc of class:
 */
public class UnsupportedTypeGuesser extends Guesser {
	private Type unsupportedType;

	// MPG Format
	private static final byte[][] mpgMagic = {{0x00, 0x00, 0x01, (byte) 0xBA, 0x21, 0x00, 0x01}};
	private static final String[] mpgExtensions = {"mpg", "mpeg"};
	private static final String[] mpgMime = {"video/mpeg", "video/mpg"};

	// AVI Format
	private static final byte[][] aviMagic = {{'R', 'I', 'F', 'F'}};
	private static final String[] aviExtensions = {"avi"};
	private static final String[] aviMime = {"video/avi"};

	// MOV Format
	private static final byte[][] movMagic = {};
	private static final String[] movExtensions = {"mov"};
	private static final String[] movMime = {"video/quicktime"};

	// Flash Format
	private static final byte[][] flashMagic =
	    {
	     {'F', 'W', 'S'},
	     {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}};
	private static final String[] flashExtensions = {"swf", "fla"};
	private static final String[] flashMime = {"application/x-shockwave-flash"};

	// Visio Format
	private static final byte[][] vsdMagic = {{(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0, (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1, 0x00}};
	private static final String[] vsdExtensions = {"vsd"};
	private static final String[] vsdMime = {"application/visio", "application/vsd"};

	// Mac Word format
	private static final byte[][] mcwMagic = {{(byte) 0xfe, (byte) 0x37}};
	private static final String[] mcwExtensions = {"mcw"};
	private static final String[] mcwMime = {"application/msword"};

	// WMV format
	private static final byte[][] wmvMagic =
	    {{0x30, 0x26, (byte) 0xB2, 0x75, (byte) 0x8E, 0x66, (byte) 0xCF, 0x11, (byte) 0xA6, (byte) 0xD9, 0x00, (byte) 0xAA, 0x00, 0x62, (byte) 0xCE,
	      0x6C}};
	private static final String[] wmvExtensions = {"wmv"};
	private static final String[] wmvMime = {"video/x-ms-wmv"};

	private UnsupportedFileTypeDescriptor[] unsupportedFileDescriptors;

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		unsupportedType = guesserManager.getPluginManager().getTypeManager().lookup(UnsupportedType.class);
		this.guesserManager = guesserManager;
		UnsupportedFileTypeDescriptor[] tempDescriptors =
		    {new UnsupportedFileTypeDescriptor(mpgExtensions, mpgMagic, mpgMime, getTypeManager().lookup(MpegType.class)),
		     new UnsupportedFileTypeDescriptor(aviExtensions, aviMagic, aviMime, getTypeManager().lookup(AviType.class)),
		     new UnsupportedFileTypeDescriptor(movExtensions, movMagic, movMime, getTypeManager().lookup(MovType.class)),
		     new UnsupportedFileTypeDescriptor(flashExtensions, flashMagic, flashMime, getTypeManager().lookup(FlashType.class)),
		     new UnsupportedFileTypeDescriptor(vsdExtensions, vsdMagic, vsdMime, getTypeManager().lookup(VisioType.class)),
		     new UnsupportedFileTypeDescriptor(mcwExtensions, mcwMagic, mcwMime, getTypeManager().lookup(MacWordType.class)),
		     new UnsupportedFileTypeDescriptor(wmvExtensions, wmvMagic, wmvMime, getTypeManager().lookup(WmvType.class))};
		unsupportedFileDescriptors = tempDescriptors;
	}

	@Override
    public Guess guess(XenaInputSource source) throws IOException, XenaException {
		List<Guess> guessList = new ArrayList<Guess>();
		for (int typeIndex = 0; typeIndex < unsupportedFileDescriptors.length; typeIndex++) {
			Guess guess = new Guess(unsupportedFileDescriptors[typeIndex].getType());

			// Get the extension...
			FileName name = new FileName(source.getSystemId());
			String extension = name.extenstionNotNull();

			boolean extMatch = false;
			if (!extension.equals("")) {
				if (unsupportedFileDescriptors[typeIndex].extensionMatch(extension)) {
					extMatch = true;
				}
			}
			guess.setExtensionMatch(extMatch);

			// Get the magic number.
			byte[] first = new byte[24];
			source.getByteStream().read(first);

			if (unsupportedFileDescriptors[typeIndex].magicNumberMatch(first)) {
				// Only set for matches - because we have at least one type without a proper magic number
				// (mov's magic number is a 0 in the 3rd position - not particularly unique!) we can't
				// set it to false.
				guess.setMagicNumber(true);
			}

			// get the mime type...
			String type = source.getMimeType();
			if (type != null && !type.equals("")) {
				if (unsupportedFileDescriptors[typeIndex].mimeTypeMatch(type)) {
					guess.setMimeMatch(true);
				}
			}

			// This guesser gets a lower priority than other guessers - want to give every chance of this file being
			// normalised properly!
			guess.setPriority(GuessPriority.LOW);

			guessList.add(guess);
		}

		Collections.sort(guessList, new GuessComparator());

		// Guesses are in ascending order of Guess Score, so we want the last in the list
		return guessList.get(guessList.size() - 1);
	}

	@Override
    public String getName() {
		return "Unsupported Types Guesser";
	}

	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return unsupportedFileDescriptors;
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType() {
		return unsupportedType;
	}

	private class GuessComparator implements Comparator<Guess> {
		public int compare(Guess o1, Guess o2) {
			return guesserManager.getGuessRanker().getRanking(o1).compareTo(guesserManager.getGuessRanker().getRanking(o2));
		}
	}

}
