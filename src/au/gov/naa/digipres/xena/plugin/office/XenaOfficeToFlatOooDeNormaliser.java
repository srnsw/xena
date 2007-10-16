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

package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;
import au.gov.naa.digipres.xena.util.UrlEncoder;

/**
 * Convert a Xena OOo file to native open document file.
 *
 */
public class XenaOfficeToFlatOooDeNormaliser extends BinaryDeNormaliser {

	private static final String EXTENSION_XPATH_STRING = "//opendocument/@extension";

	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		String elementName = OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX + ":" + OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX;
		if (elementName.equals(qName)) {
			start();
		}
	}

	@Override
    public String toString() {
		return "Office";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException {
		try {
			File inputFile = xis.getFile();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			// If the file originally had a space in it, it will have been converted to %20 in the xena file name.
			// However the parsing process assumes that this needs to be decoded, and converts it back into a space,
			// and consequently cannot find the file. We need to URL-encode the filename before we begin.
			String uri = "file:" + inputFile.getAbsolutePath();
			if (File.separatorChar == '\\') {
				uri = uri.replace('\\', '/');
			}
			String encodedURI = UrlEncoder.encode(uri);
			Document xmlDoc = builder.parse(encodedURI);

			// Extract the extension from the XML
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(EXTENSION_XPATH_STRING, xmlDoc);
		} catch (Exception e) {
			throw new XenaException("Problem retrieving file extension of normalised office file.", e);
		}
	}

}
