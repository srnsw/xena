/*
 * Created on 26/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class OrgXUnwrapper extends XMLFilterImpl {
	int packagesFound = 0;

	boolean contentFound = false;

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

		if (contentFound) {
			super.startElement(namespaceURI, localName, qName, atts);
		}
		if (qName.equals(OrgXMetaDataWrapper.ORGX_CONTENT_TAG)) {
			contentFound = true;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals(OrgXMetaDataWrapper.ORGX_CONTENT_TAG)) {
			contentFound = false;
		}
		if (contentFound) {
			super.endElement(namespaceURI, localName, qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (contentFound) {
			super.characters(ch, start, length);
		}
	}

	protected boolean pass() {
		return contentFound;
	}
}
