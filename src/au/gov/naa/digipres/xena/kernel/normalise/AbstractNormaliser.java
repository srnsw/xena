package au.gov.naa.digipres.xena.kernel.normalise;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.core.Xena;

/**
 * Normalisers may find it convenient to use this abstract class.
 * @author Chris Bitmead
 */
abstract public class AbstractNormaliser implements XMLReader {
	Map properties = new HashMap();

	org.xml.sax.ContentHandler contentHandler;

    public String getVersion(){
        return Xena.getVersion();
    }
    
	public org.xml.sax.ContentHandler getContentHandler() {
		return contentHandler;
	}

	public void setEntityResolver(EntityResolver resolver) {
	}

	public void setDTDHandler(DTDHandler handler) {
	}

	public void parse(String systemId) throws java.io.IOException, org.xml.sax.SAXException {
		parse(new InputSource(systemId));
	}

	public ErrorHandler getErrorHandler() {
		throw new java.lang.UnsupportedOperationException("Method getErrorHandler() not yet implemented.");
	}

	public DTDHandler getDTDHandler() {
		throw new java.lang.UnsupportedOperationException("Method getDTDHandler() not yet implemented.");
	}

	public EntityResolver getEntityResolver() {
		throw new java.lang.UnsupportedOperationException("Method getEntityResolver() not yet implemented.");
	}

	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	public void setFeature(String name, boolean value) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		throw new java.lang.UnsupportedOperationException("Method setFeature() not yet implemented.");
	}

	public boolean getFeature(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		throw new java.lang.UnsupportedOperationException("Method getFeature() not yet implemented.");
	}

	public Object getProperty(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
		Object rtn = properties.get(name);
		return rtn;
	}

	public void setContentHandler(org.xml.sax.ContentHandler handler) {
		contentHandler = handler;
	}

	public void setErrorHandler(ErrorHandler handler) {
	}

	/**
	 * Return a human readable name for this normaliser.
	 * @return String
	 */
	abstract public String getName();

	public String toString() {
		return getName();
	}
}
