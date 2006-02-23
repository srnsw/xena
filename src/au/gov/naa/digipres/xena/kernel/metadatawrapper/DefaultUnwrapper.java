package au.gov.naa.digipres.xena.kernel.metadatawrapper;
// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * @author Chris Bitmead
 */
public class DefaultUnwrapper extends XMLFilterImpl {
	int packagesFound = 0;

	boolean contentFound = false;


	public void startElement(String namespaceURI, String localName,
							 String qName, Attributes atts) throws SAXException {
		
        System.out.println("localName:" + localName);
        if (contentFound) {
            super.startElement(namespaceURI, localName, qName, atts);            
        }
        
        if (qName.equals("content")) {
            contentFound = true;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws
		SAXException {
		if (qName.equals("content")) {
            contentFound = false;
        }
        
        if (contentFound) {
			super.endElement(namespaceURI, localName, qName);
        }
        
        
	}

	public void characters(char[] ch, int start, int length) throws
		SAXException {
		if (contentFound) {
			super.characters(ch, start, length);
		}
	}

	protected boolean pass() {
		return contentFound;
	}
}
