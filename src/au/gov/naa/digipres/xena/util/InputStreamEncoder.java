/*
 * Created on 01/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class provides a static method for encoding an input stream into base64, giving all plugins a single, common method
 * for doing so.
 * 
 * @author justinw5
 * created 01/03/2007
 * xena
 * Short desc of class:
 */
public class InputStreamEncoder
{
    /**
     * RFC suggests max of 76 characters per line
     */
    public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

    /**
     * Base64 turns 3 characters into 4...
     */
    public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;

	public static void base64Encode(InputStream inputStream, ContentHandler contentHandler) throws IOException, SAXException
	{
		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		
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
	}
	
}
