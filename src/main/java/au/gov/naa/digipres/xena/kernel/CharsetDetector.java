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

package au.gov.naa.digipres.xena.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetector {
	public static String DEFAULT_CHARSET = "UTF-8";
	private static final int MAX_BYTES_FOR_DETECTION = 64 * 1024; // 64kB

	public static String guessCharSet(URL url) throws IOException {
		return guessCharSet(url.openStream());
	}

	/**
	 * Uses juniversal_chardet to detect the character set of the given InputStream.
	 * A maximum of 64kB of characters will be used for detection.
	 * If every character in the input sample is an ASCII character, then the US-ASCII charset will be returned.
	 * Null is returned if no matching charset is found.
	 * 
	 * 
	 * 
	 * @param is
	 * @return name of the matching charset, or null if no match could be found.
	 * @throws IOException
	 */
	public static String guessCharSet(InputStream is) throws IOException {

		UniversalDetector detector = new UniversalDetector(null);

		byte[] buf = new byte[4096];
		int iterationBytesRead = 0;

		iterationBytesRead = is.read(buf);
		int totalBytesRead = iterationBytesRead;
		boolean fileIsAscii = true;
		while (iterationBytesRead > 0 && totalBytesRead < MAX_BYTES_FOR_DETECTION && !detector.isDone()) {
			// Handle data read
			detector.handleData(buf, 0, iterationBytesRead);
			fileIsAscii = fileIsAscii && isAscii(buf, 0, iterationBytesRead);

			// Read more data
			iterationBytesRead = is.read(buf);
			totalBytesRead += iterationBytesRead;
		}
		detector.dataEnd();

		String detectedEncoding = fileIsAscii ? "US-ASCII" : detector.getDetectedCharset();
		return detectedEncoding;
	}

	/**
	 * Return true if every character in the given byte array is an ASCII character.
	 * @param bytes
	 * @param offset
	 * @param length
	 * @return true if every character in the given byte array is an ASCII character.
	 */
	private static boolean isAscii(byte[] bytes, int offset, int length) {
		for (int i = offset; i < length; i++) {
			// If a byte has the 8th bit set, it is not an ASCII character as they only occur in the first 7 bits
			if ((0x0080 & bytes[i]) != 0) {
				return false;
			}
		}
		return true;
	}

}
