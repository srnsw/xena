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

package au.gov.naa.digipres.xena.plugin.image;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.TempFileWriter;

/**
 * Normaliser to convert XML to Xena XML. Basically a no-op because random XML
 * can be considered Xena XML.
 *
 */
public class SvgNormaliser extends AbstractNormaliser {
	@Override
    public String getName() {
		return "SVG";
	}

	@Override
    public void parse(InputSource input, NormaliserResults results) throws java.io.IOException, org.xml.sax.SAXException {
		try {
			// Check SVG validity by creating a SVGDocument from the SVG file. An exception will be thrown if it is not
			// valid.
			File tempFile = TempFileWriter.createTempFile(input);
			String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parserClassName);
			documentFactory.createDocument(tempFile.toURI().toString());

			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

			// Do not load external DTDs
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// If we don't do this we get multiple startDocuments occuring
			XMLFilterImpl filter = new XMLFilterImpl() {
				@Override
                public void startDocument() {
				}

				@Override
                public void endDocument() {
				}
			};
			filter.setContentHandler(getContentHandler());
			filter.setParent(reader);
			reader.setContentHandler(filter);
			reader.parse(input);
		} catch (ParserConfigurationException x) {
			throw new SAXException(x);
		}
	}

}
