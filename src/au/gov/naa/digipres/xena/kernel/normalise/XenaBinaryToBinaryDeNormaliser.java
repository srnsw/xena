package au.gov.naa.digipres.xena.kernel.normalise;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

/**
 * Denormaliser for converting a Xena binary-object file to a plain binary object.
 *
 * @author Chris Bitmead
 */
public class XenaBinaryToBinaryDeNormaliser extends BinaryDeNormaliser {
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("binary-object:binary-object")) {
			start();
		}
	}
    
    public String toString(){
        return "XenaBinary";
    }
}
