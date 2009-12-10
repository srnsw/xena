/**
 * This file is part of xena.
 * 
 * xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.kernel.normalise;

import javax.xml.transform.Result;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Class to indicate a normaliser is actually a text normaliser, and provide a method to set the output file extension
 * @author Justin Waddell
 *
 */
public abstract class AbstractTextNormaliser extends AbstractNormaliser {

	public abstract String getOutputFileExtension();

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getContentHandler()
	 */
	@Override
	public ContentHandler getContentHandler() {
		return new TextContentHandler();
	}

	/**
	 * This class ensures that whenever the characters method is called, output escaping is disabled.
	 * Output escaping is re-enabled after the call to the superclass content handler has been made.
	 * 
	 * All other methods simply pass the call through to the superclass content handler.
	 * @author Justin Waddell
	 *
	 */
	public class TextContentHandler implements ContentHandler {

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			contentHandler.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
			contentHandler.characters(ch, start, length);
			contentHandler.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException {
			contentHandler.endDocument();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			contentHandler.endElement(uri, localName, qName);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
		 */
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			contentHandler.endPrefixMapping(prefix);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
		 */
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			contentHandler.ignorableWhitespace(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
		 */
		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			contentHandler.processingInstruction(target, data);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
		 */
		@Override
		public void setDocumentLocator(Locator locator) {
			contentHandler.setDocumentLocator(locator);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
		 */
		@Override
		public void skippedEntity(String name) throws SAXException {
			contentHandler.skippedEntity(name);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException {
			contentHandler.startDocument();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			contentHandler.startElement(uri, localName, qName, atts);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
		 */
		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			contentHandler.startPrefixMapping(prefix, uri);
		}

	}

}
