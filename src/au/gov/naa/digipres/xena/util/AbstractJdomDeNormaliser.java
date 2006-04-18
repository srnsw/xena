package au.gov.naa.digipres.xena.util;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.input.SAXHandler;
import org.xml.sax.SAXException;

/**
 * Allows denormalisation using JDOM. This should be avoided because SAX is
 * more efficient and works with bigger files. However JDOM can be easier to
 * deal with.
 * @author Chris Bitmead
 */
public abstract class AbstractJdomDeNormaliser extends SAXHandler implements TransformerHandler {
	protected Result result;

	// Constructors

	public AbstractJdomDeNormaliser() {}

	// Methods
	public abstract void denormalise(OutputStream outputStream) throws IOException;

	public void endDocument() throws SAXException {
		StreamResult sr = (StreamResult)result;
		try {
			denormalise(sr.getOutputStream());
		} catch (IOException ex) {
			throw new SAXException(ex);
		}
	}

	public void setResult(Result result) throws IllegalArgumentException {
		this.result = result;
	}

	public void setSystemId(String systemID) {
	}

	public String getSystemId() {
		return null;
	}

	public Transformer getTransformer() {
		return null;
	}

}
