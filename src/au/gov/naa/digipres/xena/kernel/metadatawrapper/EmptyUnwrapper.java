package au.gov.naa.digipres.xena.kernel.metadatawrapper;
// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * Of course, there is no xml to strip off, so, we are in a bit of a bind if we are using this class.
 * 
 *
 * @author aak
 */
public class EmptyUnwrapper extends XMLFilterImpl {
    boolean contentFound = true;


    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        
      super.startElement(namespaceURI, localName, qName, atts);            
        
    }

    public void endElement(String namespaceURI, String localName, String qName) throws
        SAXException {
            super.endElement(namespaceURI, localName, qName);
        
    }

    public void characters(char[] ch, int start, int length) throws
        SAXException {
            super.characters(ch, start, length);
    }

    protected boolean pass() {
        return contentFound;
    }
}
