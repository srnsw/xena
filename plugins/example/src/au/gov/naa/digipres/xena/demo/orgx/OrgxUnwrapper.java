/*
 * Created on 26/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;
    import org.xml.sax.Attributes;
    import org.xml.sax.SAXException;
    import org.xml.sax.helpers.XMLFilterImpl;


    public class OrgxUnwrapper extends XMLFilterImpl {

    private boolean contentFound = false;

    /*
     * If we are in the content, call super.startElement(...)
     * 
     * Otherwise, if the tag is our content tag, set contentFound to be true.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        
         if (contentFound) {
             super.startElement(namespaceURI, localName, qName, atts);            
         }
         if (qName.equals(OrgXMetaDataWrapper.ORGX_CONTENT_TAG)) {
             contentFound = true;
        }
    }

    /*
     * If we are in the content, call super.EndElement(...)
     * 
     * Otherwise, if the tag is our content tag, the content is ending,
     * so set contentFound to be false.
     */
    public void endElement(String namespaceURI, String localName, String qName) throws
        SAXException {
        if (qName.equals(OrgXMetaDataWrapper.ORGX_CONTENT_TAG)) {
             contentFound = false;
         }
         if (contentFound) {
            super.endElement(namespaceURI, localName, qName);
         }
    }

    /*
     * If we are in our content, call super.characters(...)
     * Otherwise do nothing!
     */
    public void characters(char[] ch, int start, int length) throws
        SAXException {
        if (contentFound) {
            super.characters(ch, start, length);
        }
    }
}
