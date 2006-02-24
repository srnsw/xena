package au.gov.naa.digipres.xena.plugin.basic;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

/**
 * Normalise a binary file to a Xena binary-object file.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class BinaryToXenaBinaryNormaliser extends AbstractNormaliser {
	final static String PREFIX = "binary-object";

	final static String URI = "http://preservation.naa.gov.au/binary-object/1.0";

	public String getName() {
		return "Binary";
	}

	public void parse(InputSource input) throws java.io.IOException, org.xml.sax.SAXException {
		AttributesImpl att = new AttributesImpl();
		ContentHandler ch = getContentHandler();
		ch.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, att);
		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		InputStream is = input.getByteStream();
		// 80 characters makes nice looking output
		byte[] buf = new byte[80];
		int c;
		while (0 <= (c = is.read(buf))) {
			byte[] tbuf = buf;
			if (c < buf.length) {
				tbuf = new byte[c];
				System.arraycopy(buf, 0, tbuf, 0, c);
			}
			char[] chs = encoder.encode(tbuf).toCharArray();
			ch.characters(chs, 0, chs.length);
		}
		ch.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);
	}
}
