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
 * Created on 28/02/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class OfficeDeNormaliser extends BinaryDeNormaliser {
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
		// File inputFile = xis.getFile();

		// DocumentBuilder builder =
		// DocumentBuilderFactory.newInstance().newDocumentBuilder();
		// Document xmlDoc = builder.parse(inputFile);

		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(EXTENSION_XPATH_STRING, xis);
		} catch (XPathExpressionException e) {
			throw new XenaException("Problem retrieving file extension of normalised office file.", e);
		}

	}

}
