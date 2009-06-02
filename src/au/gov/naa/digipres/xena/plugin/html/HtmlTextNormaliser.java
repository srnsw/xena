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

import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.plugin.html.util.HTMLDocumentUtilities;
import au.gov.naa.digipres.xena.util.JdomUtil;

/**
 * @author Justin Waddell
 *
 */
public class HtmlTextNormaliser extends AbstractTextNormaliser {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "HTML Text Normaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {

		ContentHandler contentHandler = getContentHandler();

		// We want to write our HTML tags raw, so we need to disable output escaping.
		contentHandler.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");

		// Ensure that only characters are written to our output
		CharactersOnlyContentHandler charactersOnlyCH = new CharactersOnlyContentHandler(contentHandler);

		try {
			Element cleanHTMLDoc = HTMLDocumentUtilities.getCleanHTMLDocument(input, normaliserManager);
			JdomUtil.writeElement(charactersOnlyCH, cleanHTMLDoc);
		} catch (TransformerConfigurationException e) {
			throw new SAXException(e);
		} catch (JDOMException e) {
			throw new SAXException(e);
		} finally {
			// Re-enable output escaping.
			contentHandler.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
		}

	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		// This normaliser simply outputs the original file, so output is a .html file
		return "txt";
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	/**
	 * Class used to ensure that only the characters of our HTML document are written out -
	 * all events involving HTML tags will be ignored.
	 * @author Justin Waddell
	 *
	 */
	private class CharactersOnlyContentHandler extends DefaultHandler {

		private static final String SCRIPT_TAG = "script";

		private ContentHandler outputContentHandler;
		private boolean inScriptElement = false;

		public CharactersOnlyContentHandler(ContentHandler outputContentHandler) {
			this.outputContentHandler = outputContentHandler;
		}

		@Override
		public void endElement(String uri, String localName, String name) {
			// We do not want to print out the contents of scripts, so we need to know when we're in a script tag
			if (SCRIPT_TAG.equals(localName.toLowerCase()) || SCRIPT_TAG.equals(name.toLowerCase())) {
				inScriptElement = false;
			}
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) {
			// We do not want to print out the contents of scripts, so we need to know when we're in a script tag
			if (SCRIPT_TAG.equals(localName.toLowerCase()) || SCRIPT_TAG.equals(name.toLowerCase())) {
				inScriptElement = true;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// Do not print out the contents of a script tag
			if (!inScriptElement) {
				// Create a string with the preceding and trailing whitespace removed
				String outputString = new String(ch, start, length).trim();

				// If the String is not empty, add an EOL and output it
				if (!outputString.equals("")) {
					outputString = outputString + "\n";
					outputContentHandler.characters(outputString.toCharArray(), 0, outputString.length());
				}
			}
		}

	}

}
