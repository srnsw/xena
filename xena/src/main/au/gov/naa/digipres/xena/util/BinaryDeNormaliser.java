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

package au.gov.naa.digipres.xena.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Denormalise a Xena package that contains base64 encoded data. By default it
 * assumes the data is under the first element. If it isn't, then override the
 * start() and end() functions to tell it when to get to work.
 */
public class BinaryDeNormaliser extends AbstractDeNormaliser {
	boolean found = false;

	sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	@Override
    public String getName() {
		return "Binary De-normaliser";
	}

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		start();
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		end();
	}

	protected void start() throws SAXException {
		found = true;
	}

	protected void end() throws SAXException {
		write();
		found = false;
	}

	protected void write() throws SAXException {
		try {
			byte[] bytes = decoder.decodeBuffer(baos.toString());
			((StreamResult) result).getOutputStream().write(bytes);
			baos.reset();
		} catch (IOException x) {
			throw new SAXException(x);
		}
	}

	@Override
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
		if (found) {
			int end = start + length;
			// We can't decode with arbitrary chunk boundaries or
			// it is corrupted. Instead decode on line boundaries.
			for (int i = start; i < end; i++) {
				baos.write(ch[i]);
				if (ch[i] == '\n') {
					write();
				}
			}
		}
	}
}
