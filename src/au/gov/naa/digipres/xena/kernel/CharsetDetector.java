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

package au.gov.naa.digipres.xena.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

public class CharsetDetector {
	public static String DEFAULT_CHARSET = "UTF-8";

	static class GuessResult {
		public boolean found = false;

		public String charset = null;

		byte[] buf;
	}

	public static String guessCharSet(URL url) throws IOException {
		return guessCharSet(url, -1);
	}

	public static String guessCharSet(URL url, long max) throws IOException {
		return guessCharSet(url.openStream(), max);

	}

	public static String mustGuessCharSet(InputStream is, long max) throws IOException {
		GuessResult res = guessCharSetPlus(is, max);
		if (!res.found) {
			throw new IOException("Cannot Guess Character Set for resource");
		}
		return res.charset;
	}

	public static String guessCharSet(InputStream is, long max) throws IOException {
		String charset = null;
		GuessResult res = guessCharSetPlus(is, max);
		if (res.found) {
			charset = res.charset;
		}
		return charset;
	}

	static GuessResult guessCharSetPlus(InputStream is, long max) throws IOException {
		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		final GuessResult guessResult = new GuessResult();
		guessResult.charset = DEFAULT_CHARSET;
		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				guessResult.found = true;
				guessResult.charset = charset;
			}
		});
		byte[] buf = new byte[4096];
		int len;
		boolean done = false;
		boolean isAscii = true;
		long total = 0;

		while ((max < 0 || total < max) && (len = is.read(buf, 0, buf.length)) != -1) {
			total += len;
			// Check if the stream is only ascii.
			if (isAscii) {
				isAscii = det.isAscii(buf, len);
				// DoIt if non-ascii and not done yet.
			}
			if (!isAscii && !done) {
				if (guessResult.buf == null) {
					guessResult.buf = new byte[len];
					System.arraycopy(buf, 0, guessResult.buf, 0, len);
				}
				done = det.DoIt(buf, len, false);
			}
		}
		det.DataEnd();
		if (isAscii) {
			guessResult.charset = "US-ASCII";
			guessResult.found = true;
		}
		return guessResult;
	}

}
