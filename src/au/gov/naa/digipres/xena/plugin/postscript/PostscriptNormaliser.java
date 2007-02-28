package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Convert postScript to the Xena postScript format.
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 * 
 * @author Kamaj Jayakantha de Mel 
 * 
 * @since 14-Feb-2007
 * @version 1.1
 * 
 */
public class PostscriptNormaliser extends AbstractNormaliser {
	/**
	 * Prefix
	 */
	final static String POSTSCRIPT_PREFIX = "postscript";

	final static String POSTSCRIPT_URI = "http://preservation.naa.gov.au/postScript/1.0";

	/**
	 * Max characters per line
	 */
	public static final int MAX_LINE_LENGTH = 76;

	/**
	 * Chunk size
	 */
	public static final int CHUNK_SIZE = (MAX_LINE_LENGTH * 3) / 4;

	/**
	 * Get normaliser name
	 * @return String : normaliser name
	 */
	@Override
	public String getName() {
		return "Postscript";
	}

	/**
	 * Parse the postscript file content into xml format.
	 * 
	 * @param InputSource, NormaliserResults
	 * @throws IOException, SAXException
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results)throws IOException, SAXException {

		// Declare content handler to parse xml of postscript xena file
		ContentHandler contentHandler = getContentHandler();
		AttributesImpl attribute = new AttributesImpl();

		// Create the first element into xena postscript file
		contentHandler.startElement(POSTSCRIPT_URI, POSTSCRIPT_PREFIX, POSTSCRIPT_PREFIX + ":" + POSTSCRIPT_PREFIX, attribute);

		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();

		InputStream inputStream = input.getByteStream();

		// An array of byte to store inputstream
		byte[] bufffer = new byte[CHUNK_SIZE];
		int count;

		// Writing inputstream from postscript file into xena postscript
		while (0 <= (count = inputStream.read(bufffer))) {

			byte[] tmpbuffer = bufffer;
			if (count < bufffer.length) {
				tmpbuffer = new byte[count];
				System.arraycopy(bufffer, 0, tmpbuffer, 0, count);
			}
			char[] chs = encoder.encode(tmpbuffer).toCharArray();
			contentHandler.characters(chs, 0, chs.length);

		}
		// Create the end element in xena postscript file
		contentHandler.endElement(POSTSCRIPT_URI, POSTSCRIPT_PREFIX,POSTSCRIPT_PREFIX + ":" + POSTSCRIPT_PREFIX);
	}
}
