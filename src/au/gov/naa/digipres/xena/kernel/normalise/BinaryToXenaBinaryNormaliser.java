package au.gov.naa.digipres.xena.kernel.normalise;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.util.InputStreamEncoder;


/**
 * Normalise a binary file to a Xena binary-object file.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class BinaryToXenaBinaryNormaliser extends AbstractNormaliser 
{
	public final static String BINARY_NORMALISER_NAME = "Binary";
	
	final static String PREFIX = "binary-object";
	final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
	final static String DESCRIPTION = "The following data is a MIME-compliant (RFC 2045) PEM base64 (RFC 1421) representation of the original file contents.";

	final static String URI = "http://preservation.naa.gov.au/binary-object/1.0";
	
	public String getName() 
	{
		return BINARY_NORMALISER_NAME;
	}

	public void parse(InputSource input, NormaliserResults nr) throws IOException, SAXException 
	{
		AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, PROCESS_DESCRIPTION_TAG_NAME, PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
		ContentHandler contentHandler = getContentHandler();
		InputStream inputStream = input.getByteStream();
		
		contentHandler.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, attributes);
		InputStreamEncoder.base64Encode(inputStream, contentHandler);
		contentHandler.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);
	}
}
