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
 * Created on 06/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class XmlDeNormaliser extends AbstractDeNormaliser {

	public static final String XHTML_DOCTYPE_NAME = "html";
	public static final String XHTML_DOCTYPE_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Transitional//EN";
	public static final String XHTML_DOCTYPE_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";

	private TransformerHandler outputXMLWriter;

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "XML DeNormaliser";
	}

	/**
	 * 
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) {
		return "xml";
	}

	/**
	 * Get the Doctype Name to be used for the output XML document.
	 * The default is to return null, which will mean a doctype is not output.
	 * Subclasses should override this method if a doctype is to be output.
	 * @return
	 */
	public String getDoctypeName() {
		// We are not currently using a doctype for generic XML
		return null;
	}

	/**
	 * Get the Doctype Public Identifier to be used for the output XML document.
	 * The default is to return null, which will mean a doctype is not output.
	 * Subclasses should override this method if a doctype is to be output.
	 * @return
	 */
	public String getDoctypePublicIdentifier() {
		// We are not currently using a doctype for generic XML
		return null;
	}

	/**
	 * Get the Doctype System Identifier to be used for the output XML document.
	 * The default is to return null, which will mean a doctype is not output.
	 * Subclasses should override this method if a doctype is to be output.
	 * @return
	 */
	public String getDoctypeSystemIdentifier() {
		// We are not currently using a doctype for generic XML
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// create our transform handler
		outputXMLWriter = null;
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		try {
			outputXMLWriter = transformFactory.newTransformerHandler();

			// Output a doctype if required
			if (getDoctypePublicIdentifier() != null) {
				outputXMLWriter.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, getDoctypePublicIdentifier());
			}
		} catch (TransformerConfigurationException e) {
			throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
		}

		if (streamResult == null) {
			throw new SAXException("StreamResult has not been initialised.");
		}

		outputXMLWriter.setResult(streamResult);
		outputXMLWriter.startDocument();

		// Output a doctype if required
		if (getDoctypePublicIdentifier() != null) {
			outputXMLWriter.startDTD(getDoctypeName(), getDoctypePublicIdentifier(), getDoctypeSystemIdentifier());
		}

	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		outputXMLWriter.characters(ch, start, length);
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		outputXMLWriter.endDocument();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		outputXMLWriter.endElement(namespaceURI, localName, qName);
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		outputXMLWriter.startElement(namespaceURI, localName, qName, atts);
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		outputXMLWriter.comment(ch, start, length);
	}

}
