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

/*
 * Created on 21/02/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.csv;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class XenaCsvToCsvDenormaliser extends AbstractDeNormaliser {
	/**
	 * This denormaliser is for the new CSV file format - almost identical to plaintext.
	 * basically, the xml looks like this:
	 * 
	 * <package>
	 *      <meta_data>
	 *          </some_meta_data_information>
	 *      </meta_data>
	 *      <csv:csv>
	 *          <csv:line> CSV DATA  </csv:line>
	 *          <csv:line> MORE DATA </csv:line>
	 *          .
	 *            .
	 *              .
	 *          <csv:line> LAST BIT OF DATA </csv:line>
	 *      </csv:csv>
	 *      <meta_data>
	 *          </more_different_meta_data_information>
	 *      </meta_data>
	 * </package>
	 * 
	 * So, once we get to the <csv:csv> tag, we will be getting a whole bunch of csv line tags, and we
	 * just write those out, line by line, to our output file!
	 * 
	 */
	static final String CONTENT_NAME = "csv:csv";

	private BufferedWriter bufferedWriter;
	private boolean insideCsvTag = false;

	@Override
    public String getName() {
		return "CSV Denormaliser";
	}

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (qName.equals(CONTENT_NAME)) {
			insideCsvTag = true;
		}
	}

	@Override
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (qName.equals(CONTENT_NAME)) {
			try {
				bufferedWriter.newLine();
			} catch (IOException x) {
				throw new SAXException(x);
			}
			insideCsvTag = false;
		}
	}

	@Override
    public void characters(char[] ch, int offset, int len) throws org.xml.sax.SAXException {
		assert bufferedWriter != null : "characters: buffered writer is null";
		if (insideCsvTag) {
			try {
				bufferedWriter.write(ch, offset, len);
			} catch (IOException x) {
				throw new SAXException(x);
			}
		}
	}

	@Override
    public void startDocument() throws org.xml.sax.SAXException {
		bufferedWriter = new BufferedWriter(((StreamResult) result).getWriter());
	}

	@Override
    public void endDocument() throws org.xml.sax.SAXException {
		assert bufferedWriter != null : "endDocument: buffered writer is null";
		try {
			bufferedWriter.close();
		} catch (IOException x) {
			throw new SAXException(x);
		}
		bufferedWriter = null;
	}

}
