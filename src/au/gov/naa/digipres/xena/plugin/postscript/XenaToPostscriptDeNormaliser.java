package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.BufferedWriter;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * This is able to De-Normalise Xena files to Postscript file. It contains a
 * several denoramliser specific abstract methods.
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 * 
 * @authors Quang Phuc Tran(Eric) and Kamaj Jayakantha de Mel
 * 
 * @since 26-Feb-2007
 * @version 1.0
 *
 */

public class XenaToPostscriptDeNormaliser extends AbstractDeNormaliser {

	@Override
	/**
	 * Return Postscript Denormaliser name
	 */
	public String getName() {
		
		return "Postscript Denormaliser";
	}

	//Buffer used to write to output file
	private BufferedWriter bufferedWriter;

	XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();

	StringBuffer stringBuffer = new StringBuffer();

	byte[] result;
	
	//Delare Postscript content tag so that we can parse content
	static final String CONTENT_TAG = "postscript:postscript";
	
	//Flag to indicate we are in Postscript content tag or not
	private boolean inPostscriptPart = false;

	 
	
	/**
	 * Start to recognise the Postscript content tag and return flag to true
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (qName.equals(CONTENT_TAG)) {
			inPostscriptPart = true;
		}
	}

	/**
	 * Close the Postscript content tag and return flag to false
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
		assert qName != null : "qName is not set";
		if (qName.equals(CONTENT_TAG)) {
			try {
				bufferedWriter.newLine();
			} catch (IOException x) {
				throw new SAXException(x);
			}
			inPostscriptPart = false;
		}
	}
	
	
	/**
	 * Write out the content to output file
	 */
	public void characters(char[] ch, int offset, int len) throws org.xml.sax.SAXException {
		assert bufferedWriter != null : "characters: bufferedWriter is null";
		if (inPostscriptPart) {
			try {
				bufferedWriter.write(ch, offset, len);
			} catch (IOException x) {
				throw new SAXException(x);
			}
		}
	}
	
	
	
	/**
	 * Actual parsing xml
	 */
	@Override
    public void startDocument() throws org.xml.sax.SAXException {
        if (streamResult == null) {
            throw new SAXException("StreamResult not initialised by the normaliser manager");
        }
        bufferedWriter = new BufferedWriter(streamResult.getWriter());
        try {
            char[] PostscriptMagic = new char[PostscriptGuesser.POSTSCRIPT_FILE_SIGNATURE_LENGTH];
            for (int i = 0; i < PostscriptGuesser.POSTSCRIPT_FILE_SIGNATURE_LENGTH; i++) {
                PostscriptMagic[i] = (char)PostscriptGuesser.POSTSCRIPT_FILE_SIGNATURE[i];
            }
            bufferedWriter.write(PostscriptMagic);
        } catch (IOException x) {
            throw new SAXException(x);
        }
    }
	
	
	/**
	 * 
	 */
	 @Override
	    public void endDocument() throws org.xml.sax.SAXException {
	        try {
	            bufferedWriter.close();
	        } catch (IOException x) {
	            throw new SAXException(x);
	        }
	        bufferedWriter = null;
	    }
}
