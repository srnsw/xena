package au.gov.naa.digipres.xena.plugin.basic;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Normalises basic types like strings, integers etc.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
abstract public class BasicTypeNormaliser extends AbstractNormaliser {
	final static String INTEGER_URI = "http://preservation.naa.gov.au/integer/1.0";

	final static String STRING_URI = "http://preservation.naa.gov.au/string/1.0";

	public BasicTypeNormaliser() {
	}

	public void parse(InputSource input) throws java.io.IOException, org.xml.sax.SAXException {
		
		Logger logger = Logger.getLogger(this.getClass().getName());
		
		if (input.getEncoding() == null) {
			try {
				input.setEncoding(CharsetDetector.mustGuessCharSet(input.getByteStream(), 2 ^ 16));
			} catch (IOException x) {
				logger.log(Level.FINEST,
				           "Warning: could not guess charset, " +
				           "using default - source: " + input.getSystemId(),
				           x);
				input.setEncoding(CharsetDetector.DEFAULT_CHARSET);
			}
		}

		Type type = ((XenaInputSource)input).getType();
		ContentHandler ch = this.getContentHandler();
		AttributesImpl att = new AttributesImpl();
		att.addAttribute("http://www.w3.org/XML/1998/namespace", "space", "xml:space", null, "preserve");
		if (type instanceof IntegerFileType) {
			ch.startElement(INTEGER_URI, "integer", "integer:integer", att);
		} else {
			ch.startElement(STRING_URI, "string", "string:string", att);
		}
		char[] bytes = new char[4096];
		Reader is = input.getCharacterStream();
		int c;
		while (0 <= (c = is.read(bytes, 0, bytes.length))) {
			ch.characters(bytes, 0, c);
		}
		if (type instanceof IntegerFileType) {
			ch.endElement(INTEGER_URI, "integer", "integer:integer");
		} else {
			ch.endElement(STRING_URI, "string", "string:string");
		}
	}
}
