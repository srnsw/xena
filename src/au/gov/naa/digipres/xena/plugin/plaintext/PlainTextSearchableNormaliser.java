/**
 * This file is part of plaintext.
 * 
 * plaintext is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * plaintext is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with plaintext; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.plaintext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Class to produce a searchable file from the given input. As plaintext is inherently searchable,
 * we will just be writing the file out directly.
 * 
 * @author Justin Waddell
 *
 */
public class PlainTextSearchableNormaliser extends AbstractSearchableNormaliser {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "Plain Text Searchable Normaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		InputStream inputStream = input.getByteStream();
		inputStream.mark(Integer.MAX_VALUE);
		if (input.getEncoding() == null) {
			input.setEncoding(CharsetDetector.mustGuessCharSet(inputStream, 2 ^ 16));
		}
		inputStream.reset();
		ContentHandler contentHandler = getContentHandler();
		Reader reader = input.getCharacterStream();
		char[] buffer = new char[10 * 1024];
		int charsRead = reader.read(buffer);
		while (charsRead > 0) {
			contentHandler.characters(buffer, 0, charsRead);
			charsRead = reader.read(buffer);
		}
	}

}
