package au.gov.naa.digipres.xena.plugin.naa;
// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * @author Chris Bitmead
 */
public class NaaPackageUnwrapFilter extends XMLFilterImpl {
	int packagesFound = 0;

	boolean contentFound = false;

	boolean nextFound = false;

    public String toString(){
        return "NAA Package - Unwrapper. Looking for package:package and package:content";
    }
    
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		//notout naa unwrapper...
        //System.out.println("in naa unwrapper start element.. " + localName + " and " + qName);
        if (2 <= packagesFound) {
			if (contentFound) {
				nextFound = true;
			} else if (qName.equals("package:content")) {
				contentFound = true;
			}
		} else if (qName.equals("package:package")) {
			packagesFound++;
		}
		if (pass()) {
			super.startElement(namespaceURI, localName, qName, atts);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws
		SAXException {
		if (qName.equals("package:content")) {
			packagesFound--;
			nextFound = false;
			contentFound = false;
		}
		if (pass()) {
			super.endElement(namespaceURI, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length) throws
		SAXException {
		if (pass()) {
			super.characters(ch, start, length);
		}
	}

	protected boolean pass() {
		return nextFound && 2 <= packagesFound;
	}
}
