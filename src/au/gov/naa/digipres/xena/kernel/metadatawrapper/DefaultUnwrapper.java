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

package au.gov.naa.digipres.xena.kernel.metadatawrapper;

// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 */
public class DefaultUnwrapper extends XMLFilterImpl {
	int packagesFound = 0;

	boolean contentFound = false;

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

		if (contentFound) {
			super.startElement(namespaceURI, localName, qName, atts);
		}
		if (DefaultWrapper.CONTENT_TAG.equals(qName)) {
			contentFound = true;
		}
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (DefaultWrapper.CONTENT_TAG.equals(qName)) {
			contentFound = false;
		}
		if (contentFound) {
			super.endElement(namespaceURI, localName, qName);
		}
	}

	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
		if (contentFound) {
			super.characters(ch, start, length);
		}
	}
}
