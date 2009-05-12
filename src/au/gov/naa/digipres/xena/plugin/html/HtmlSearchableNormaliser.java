/**
 * This file is part of html.
 * 
 * html is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * html is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with html; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Result;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * @author Justin Waddell
 *
 */
public class HtmlSearchableNormaliser extends AbstractSearchableNormaliser {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "HTML Searchable Normaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		// Most indexers should be able to handle raw HTML, so we will just write out the original file
		InputStream inputStream = input.getByteStream();
		inputStream.mark(Integer.MAX_VALUE);
		inputStream.reset();
		ContentHandler contentHandler = getContentHandler();
		Reader reader = input.getCharacterStream();
		char[] buffer = new char[10 * 1024];
		int charsRead = reader.read(buffer);
		// We want to write our HTML tags raw, so we need to disable output escaping.
		contentHandler.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
		while (charsRead > 0) {
			contentHandler.characters(buffer, 0, charsRead);
			charsRead = reader.read(buffer);
		}
		// Re-enable output escaping.
		contentHandler.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractSearchableNormaliser#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		// This normaliser simply outputs the original file, so output is a .html file
		return "html";
	}

}
