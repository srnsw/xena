package au.gov.naa.digipres.xena.helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Denormalise a Xena package that contains base64 encoded data. By default it
 * assumes the data is under the first element. If it isn't, then override the
 * start() and end() functions to tell it when to get to work.
 */
public class BinaryDeNormaliser extends AbstractDeNormaliser {
	boolean found = false;

	sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    public String getName(){
        return "Binary De-normaliser";
    }

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		start();
	}

	public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		end();
	}

	protected void start() throws SAXException {
		found = true;
	}

	protected void end() throws SAXException {
		write();
		found = false;
	}

	protected void write() throws SAXException {
		try {
			byte[] bytes = decoder.decodeBuffer(baos.toString());
			((StreamResult)result).getOutputStream().write(bytes);
			baos.reset();
		} catch (IOException x) {
			throw new SAXException(x);
		}
	}

	public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
		if (found) {
			int end = start + length;
			// We can't decode with arbitrary chunk boundaries or
			// it is corrupted. Instead decode on line boundaries.
			for (int i = start; i < end; i++) {
				baos.write(ch[i]);
				if (ch[i] == '\n') {
					write();
				}
			}
		}
	}
}
