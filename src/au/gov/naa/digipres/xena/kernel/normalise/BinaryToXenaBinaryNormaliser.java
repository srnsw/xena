package au.gov.naa.digipres.xena.kernel.normalise;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;


/**
 * Normalise a binary file to a Xena binary-object file.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class BinaryToXenaBinaryNormaliser extends AbstractNormaliser {
	final static String PREFIX = "binary-object";
	final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
	final static String DESCRIPTION = "The following data is a MIME-compliant (RFC 1421) PEM base64 (RFC 1421) representation of the original file contents.";

	final static String URI = "http://preservation.naa.gov.au/binary-object/1.0";

    /**
     * RFC suggests max of 76 characters per line
     */
    public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

    /**
     * Base64 turns 3 characters into 4...
     */
    public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;
	
	public String getName() {
		return "Binary";
	}

	public void parse(InputSource input, NormaliserResults nr) throws java.io.IOException, org.xml.sax.SAXException {
		AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, PROCESS_DESCRIPTION_TAG_NAME, PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
		ContentHandler contentHandler = getContentHandler();
		contentHandler.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, attributes);
		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		InputStream inputStream = input.getByteStream();
		
		byte[] readBuffer = new byte[CHUNK_SIZE];
		int charsRead;
		while (0 <= (charsRead = inputStream.read(readBuffer))) {
			byte[] outputBuffer = readBuffer;
			if (charsRead < readBuffer.length) {
				outputBuffer = new byte[charsRead];
				System.arraycopy(readBuffer, 0, outputBuffer, 0, charsRead);
			}
			
			// Encode output with base64 encoding, and write out.
			// The output needs to be trimmed so we can remove the carriage return character, 
			// which otherwise gets encoded into the XML
			char[] encodedChars = encoder.encode(outputBuffer).trim().toCharArray();
			contentHandler.characters(encodedChars, 0, encodedChars.length);
			
			// Print EOL character in order to conform to MIME base64 specification
			char[] eolCharArr = "\n".toCharArray();
			contentHandler.characters(eolCharArr, 0, eolCharArr.length);
		}
		contentHandler.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);
	}
}
