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

/*
 * Created on 24/04/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.FoundException;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public class TagContentFinder {

	/**
	 * Find the contents of a tag using 
	 * @param input
	 * @param tag
	 * @return
	 * @throws XenaException
	 */
	// TODO: aak - TagContentFinder - do this better in a more efficient way. That would be good.
	static public String getTagContents(XenaInputSource input, String tag) throws XenaException {
		final String myTag = tag;
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setContentHandler(new XMLFilterImpl() {
				String result = "";
				boolean found = false;

				@Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					// Bail out early as soon as we've found what we want
					// for super efficiency.

					if (myTag.equals(qName)) {
						found = true;
					}
				}

				@Override
                public void characters(char ch[], int start, int length) throws SAXException {
					if (found) {
						result += new String(ch, start, length);
					}
				}

				@Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
					if (found) {
						throw new FoundException(result);
					}
				}
			});
			try {
				reader.parse(input);
			} catch (FoundException x) {
				input.close();
				return x.getName();
			}
			input.close();
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		}
		return null;
	}

}
