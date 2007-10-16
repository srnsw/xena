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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Wrapper for an AIP. This wrapper is only used once per AIP, and is placed at the outermost level
 * of the XML file. Its purpose is to contain the signature tag - a checksum produced from the content
 * contained within this wrapper.
 *
 * @authoer Justin Waddell
 */
public class NaaOuterWrapNormaliser extends XMLFilterImpl {

	private ContentHandler checksumHandler;
	private ByteArrayOutputStream checksumBAOS;
	private MessageDigest md5Creator;
	private OutputStreamWriter checksumOSW;

	private String description =
	    "This checksum is created from the entire contents of the " + NaaTagNames.WRAPPER_AIP + " tag, not including the tag itself";

	@Override
    public String toString() {
		return "NAA Package Wrap Outer";
	}

	/**
	 * Opens the signed-aip and aip tags, and intialises the checksum producing system.
	 */
	@Override
    public void startDocument() throws org.xml.sax.SAXException {
		super.startDocument();
		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.startElement(NaaTagNames.WRAPPER_URI, NaaTagNames.SIGNED_AIP, NaaTagNames.WRAPPER_SIGNED_AIP, att);
		th.startElement(NaaTagNames.WRAPPER_URI, NaaTagNames.AIP, NaaTagNames.WRAPPER_AIP, att);

		// Setup checksum stream and checksum producer
		try {
			checksumBAOS = new ByteArrayOutputStream();
			checksumHandler = createChecksumHandler(checksumBAOS);
			md5Creator = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			throw new SAXException("Could not create checksum handler", e);
		}

	}

	/**
	 * Calculates the checksum and writes it to the signature tag.
	 */
	@Override
    public void endDocument() throws org.xml.sax.SAXException {
		ContentHandler th = getContentHandler();
		th.endElement(NaaTagNames.WRAPPER_URI, NaaTagNames.AIP, NaaTagNames.WRAPPER_AIP);

		// Add the checksum element
		if (md5Creator != null) {
			checksumHandler.endDocument();
			AttributesImpl atts = new AttributesImpl();
			th.startElement(NaaTagNames.WRAPPER_URI, NaaTagNames.META, NaaTagNames.WRAPPER_META, atts);

			atts.addAttribute(NaaTagNames.WRAPPER_URI, "description", "description", "CDATA", description);
			atts.addAttribute(NaaTagNames.WRAPPER_URI, "algorithm", "algorithm", "CDATA", "MD5");
			th.startElement(NaaTagNames.WRAPPER_URI, NaaTagNames.SIGNATURE, NaaTagNames.WRAPPER_SIGNATURE, atts);
			char[] signatureVal = convertToHex(md5Creator.digest()).toCharArray();
			th.characters(signatureVal, 0, signatureVal.length);
			th.endElement(NaaTagNames.WRAPPER_URI, NaaTagNames.SIGNATURE, NaaTagNames.WRAPPER_SIGNATURE);

			th.endElement(NaaTagNames.WRAPPER_URI, NaaTagNames.META, NaaTagNames.WRAPPER_META);
		}

		th.endElement(NaaTagNames.WRAPPER_URI, NaaTagNames.SIGNED_AIP, NaaTagNames.WRAPPER_SIGNED_AIP);

		try {
			if (checksumBAOS != null)
				checksumBAOS.close();
			if (checksumOSW != null)
				checksumOSW.close();
		} catch (IOException e) {
			throw new SAXException("Could not close checksum streams", e);
		}

		super.endDocument();

	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		checksumHandler.characters(ch, start, length);

		// Update MD5 checksum creator with new bytes from the call to characters
		try {
			checksumOSW.flush();
			checksumBAOS.flush();
			md5Creator.update(checksumBAOS.toByteArray());
			checksumBAOS.reset();
		} catch (IOException iex) {
			throw new SAXException("Problem updating MD5 checksum", iex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		checksumHandler.endElement(uri, localName, qName);

		// Update MD5 checksum creator with new bytes from the call to endElement
		try {
			checksumOSW.flush();
			checksumBAOS.flush();
			md5Creator.update(checksumBAOS.toByteArray());
			checksumBAOS.reset();
		} catch (IOException iex) {
			throw new SAXException("Problem updating MD5 checksum", iex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		super.startElement(uri, localName, qName, atts);
		checksumHandler.startElement(uri, localName, qName, atts);

		// Update MD5 checksum creator with new bytes from the call to startElement
		try {
			checksumOSW.flush();
			checksumBAOS.flush();
			md5Creator.update(checksumBAOS.toByteArray());
			checksumBAOS.reset();
		} catch (IOException iex) {
			throw new SAXException("Problem updating MD5 checksum", iex);
		}

	}

	private ContentHandler createChecksumHandler(ByteArrayOutputStream baos) throws IOException, TransformerException {
		// create our transform handler
		TransformerHandler transformerHandler = null;
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		transformerHandler = transformFactory.newTransformerHandler();

		checksumOSW = new OutputStreamWriter(baos, "UTF-8");
		StreamResult streamResult = new StreamResult(checksumOSW);
		transformerHandler.setResult(streamResult);
		transformerHandler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		return transformerHandler;
	}

	private static String convertToHex(byte[] byteArray) {
		/*
		 * ------------------------------------------------------ Converts byte array to printable hexadecimal string.
		 * eg convert created MD5 checksum to MD5 file form. ------------------------------------------------------
		 */
		String s; // work string for single byte translation
		String hexString = ""; // the output string being built

		for (int i = 0; i < byteArray.length; i++) {
			s = Integer.toHexString(byteArray[i] & 0xFF); // mask removes 'ffff' prefix from -ive numbers
			if (s.length() == 1)
				s = "0" + s;
			hexString = hexString + s;
		}
		return hexString;
	}

}
