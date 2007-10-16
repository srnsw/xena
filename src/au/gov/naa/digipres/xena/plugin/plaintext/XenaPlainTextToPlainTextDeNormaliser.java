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

package au.gov.naa.digipres.xena.plugin.plaintext;

// SAX classes.
import java.io.BufferedWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Denormalise Xena plaintext instances to native plaintext files.
 *
 */
public class XenaPlainTextToPlainTextDeNormaliser extends AbstractDeNormaliser {
	static final String TEXTNAME = "plaintext:line";

	BufferedWriter bw;

	boolean text = false;

	@Override
    public String getName() {
		return "Plaintext Denormaliser";
	}

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (TEXTNAME.equals(qName)) {
			text = true;
		}
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (TEXTNAME.equals(qName)) {
			try {
				bw.newLine();
			} catch (IOException x) {
				throw new SAXException(x);
			}
			text = false;
		}
	}

	@Override
    public void characters(char[] ch, int offset, int len) throws org.xml.sax.SAXException {
		assert bw != null : "characters: bw is null";
		if (text) {
			try {
				bw.write(ch, offset, len);
			} catch (IOException x) {
				throw new SAXException(x);
			}
		}
	}

	@Override
    public void startDocument() throws org.xml.sax.SAXException {
		bw = new BufferedWriter(((StreamResult) result).getWriter());
	}

	@Override
    public void endDocument() throws org.xml.sax.SAXException {
		assert bw != null : "endDocument: bw is null";
		try {
			bw.close();
		} catch (IOException x) {
			throw new SAXException(x);
		}
		bw = null;
	}
}
