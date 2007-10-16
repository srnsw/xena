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

package au.gov.naa.digipres.xena.plugin.naa;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.Ostermiller.util.MD5;

/**
 * Calculate an MD5 checksum for XML streaming through.
 *
 */
public class ChecksumContentHandler extends XMLFilterImpl {
	MD5 md5;

	public ChecksumContentHandler() {
		md5 = new MD5();
	}

	public MD5 getMD5() {
		return md5;
	}

	@Override
    public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		String name = qName;
		if (name == null) {
			name = localName;
		}
		md5.update("<");
		md5.update(name);
		for (int i = 0; i < atts.getLength(); i++) {
			name = atts.getQName(i);
			if (name == null) {
				name = atts.getLocalName(i);
			}
			md5.update(" ");
			md5.update(name);
			md5.update("=");
			md5.update(atts.getValue(i));
		}
		md5.update(">");
		super.startElement(namespaceURI, localName, qName, atts);
	}

	@Override
    public void characters(char[] ch, int start, int length) throws SAXException {
		md5.update(new String(ch, start, length));
		super.characters(ch, start, length);
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		super.endElement(namespaceURI, localName, qName);
		String name = qName;
		if (name == null) {
			name = localName;
		}
		md5.update("</");
		md5.update(name);
		md5.update(">");
	}
}
