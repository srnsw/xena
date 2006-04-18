package au.gov.naa.digipres.xena.kernel.normalise;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * AbstractDeNormaliser is an empty implementation of TransformerHandler
 * that is able to be overridden to De-Normalise Xena files. It contains a
 * several denoramliser specific abstract methods that must be implemented
 * by any new denormaliser.
 * 
 * 
 * @author Andrew Keeling
 * @author Justin Waddell
 * @author Chris Bitmead
 * 
 */
public abstract class AbstractDeNormaliser implements TransformerHandler {

    protected StreamResult streamResult;
    protected Result result;
    protected NormaliserManager normaliserManager;


    
    
    public void setResult(Result result) {
        if (result instanceof StreamResult) {
            this.result = result;
        }
    }

    /**
     * AAK
     * This method is called from normaliserManager to set the Stream Result
     * for this abstract denormaliser. This seems to break good object oriented
     * coding, since StreamResult extends Result, however, the NormaliserManager
     * export method requires that there be a stream result rather than just a
     * result. As such, the abstract denormaliser
     * 
     * @param streamResult
     */
    public void setStreamResult(StreamResult streamResult) {
        this.streamResult = streamResult;
        this.result = streamResult;
    }

    
    public StreamResult getStreamResult() {
        return streamResult;
    }
    
    
    /**
     * Return a human readable name for this normaliser.
     * 
     * @return String
     */
    abstract public String getName();

    public String toString() {
        return getName();
    }
    
    /**
     * @return Returns the normaliserManager.
     */
    public NormaliserManager getNormaliserManager() {
        return normaliserManager;
    }

    /**
     * @param normaliserManager
     *            The new value to set normaliserManager to.
     */
    public void setNormaliserManager(NormaliserManager normaliserManager) {
        this.normaliserManager = normaliserManager;
    }

    public void setSystemId(String systemID) {
    }

    public String getSystemId() {
        return null;
    }

    public Transformer getTransformer() {
        return null;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
    }

    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
    }

}