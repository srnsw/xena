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

package au.gov.naa.digipres.xena.kernel.metadatawrapper;

// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * Of course, there is no xml to strip off, so, we are in a bit of a bind if we are using this class.
 * 
 *
 */
public class EmptyUnwrapper extends XMLFilterImpl {
	boolean contentFound = true;

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

		super.startElement(namespaceURI, localName, qName, atts);

	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		super.endElement(namespaceURI, localName, qName);

	}

	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
	}

	protected boolean pass() {
		return contentFound;
	}
}
