package au.gov.naa.digipres.xena.plugin.plaintext;
// SAX classes.
import java.io.BufferedWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Denormalise Xena plaintext instances to native plaintext files.
 *
 * @author Chris Bitmead
 */
public class XenaPlainTextToPlainTextDeNormaliser extends AbstractDeNormaliser {
	static final String TEXTNAME = "plaintext:line";

	BufferedWriter bw;

	boolean text = false;

    public String getName(){
        return "Plaintext Denormaliser";
    }
        
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (TEXTNAME.equals( qName )) {
			text = true;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (TEXTNAME.equals( qName )) {
			try {
				bw.newLine();
			} catch (IOException x) {
				throw new SAXException(x);
			}
			text = false;
		}
	}

	public void characters(char[] ch, int offset, int len) throws org.xml.sax.SAXException {
		assert bw != null : "characters: bw is null";
		if (text) {
			try {
				bw.write(ch, offset, len);
			} catch (IOException x) {
				throw new SAXException(x);
			}
		}
	}

	public void startDocument() throws org.xml.sax.SAXException {
		bw = new BufferedWriter(((StreamResult)result).getWriter());
	}

	public void endDocument() throws org.xml.sax.SAXException {
		assert bw != null : "endDocument: bw is null";
		try {
			bw.close();
		} catch (IOException x) {
			throw new SAXException(x);
		}
		bw = null;
	}
}
