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

package au.gov.naa.digipres.xena.plugin.plaintext;

import java.io.IOException;
import java.io.InputStreamReader;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Guesser for plaintext files.
 *
 */
public class PlainTextGuesser extends Guesser {

	// made array of extensions so we can just add them willy - nilly.
	public static final String[] EXTENSIONS = {"txt", "log", "inf", "ini", "css", "asp", "jsp", "js", "java", "c", "cpp", "cs", "dat", "bat"};

	public static final String[] STANDARD_CHARSETS = {"US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-8"};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PlainTextGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager manager) throws XenaException {
		guesserManager = manager;
		type = getTypeManager().lookup(PlainTextFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) {
		Guess guess = new Guess(type);
		// If path ends with "/" it is really a directory, but the Sun
		// directory handler sets the mime type and returns plain text.

		// always return true! pretty much every file can be viewed as plain text, its just they might not look very
		// good...
		guess.setPossible(true);

		String mimeType = source.getMimeType();
		if (!source.getSystemId().endsWith("/") && mimeType != null && "text/plain".equals(mimeType)) {
			guess.setMimeMatch(true);
		}

		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull().toLowerCase();
		boolean extensionMatch = false;

		for (String element : EXTENSIONS) {
			if (extension.equals(element)) {
				extensionMatch = true;
				break;
			}
		}
		guess.setExtensionMatch(extensionMatch);

		// Guess the charset. If the guessed charset is not one
		// of the standard charsets, then it is not a PlainTextFile
		// (it might be a NonStandardPlainTextFile).
		try {
			String charset = CharsetDetector.mustGuessCharSet(source.getByteStream(), 2 ^ 16);
			if (charset != null && arrayContainsValue(STANDARD_CHARSETS, charset)) {
				// Check for non-plaintext chars. Test the first 64k characters with against the set of characters valid
				// for use in XML.
				// If a character is found which is not valid in XML, this is most likely not a plaintext file.
				char[] testChars = new char[64 * 1024];
				int charsRead = new InputStreamReader(source.getByteStream(), charset).read(testChars);

				boolean blockIsPlaintext = XMLCharacterValidator.isValidBlock(testChars, charsRead);

				guess.setDataMatch(blockIsPlaintext);
			}
		} catch (IOException x) {
			// throw new XenaException(x);
			// TODO: aak - plaintext guesser - Check this stuff....
			// OK - Here's the deal. If something breaks during charset detection, lets just say
			// it is borked - and return a guess that is datamatch = false.
			// then the guesser manager will put something else up. also, if required, we can
			// still go and set the normaliser to plain text regardless anyhow.

			guess.setDataMatch(false);
		}
		return guess;
	}

	@Override
	public String getName() {
		return "PlainTextGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setPossible(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	private boolean arrayContainsValue(String[] array, String value) {
		boolean found = false;
		for (String element : array) {
			if (element.equals(value)) {
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public Type getType() {
		return type;
	}

}
