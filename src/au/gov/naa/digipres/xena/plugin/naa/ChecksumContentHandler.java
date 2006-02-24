package au.gov.naa.digipres.xena.plugin.naa;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.Ostermiller.util.MD5;

/**
 * Calculate an MD5 checksum for XML streaming through.
 *
 * @author Chris Bitmead
 */
public class ChecksumContentHandler extends XMLFilterImpl {
	MD5 md5;

	public ChecksumContentHandler() {
		md5 = new MD5();
	}

	public MD5 getMD5() {
		return md5;
	}

	public void startDocument() throws SAXException {
		super.startDocument();
	}

	public void startElement(String namespaceURI, String localName,
							 String qName, Attributes atts) throws SAXException {
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

	public void characters(char[] ch, int start, int length) throws
		SAXException {
		md5.update(new String(ch, start, length));
		super.characters(ch, start, length);
	}

	public void endElement(String namespaceURI, String localName,
						   String qName) throws SAXException {

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
