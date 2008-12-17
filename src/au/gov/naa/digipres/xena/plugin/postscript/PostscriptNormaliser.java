package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;

/**
 * Convert postScript to the Xena postScript format.
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 * 
 * @author Kamaj Jayakantha de Mel 
 * @author Matthew Oliver
 * 
 * @since 14-Feb-2007
 * @version 1.2
 * 
 */

public class PostscriptNormaliser extends AbstractNormaliser {
	/**
	 * Prefix
	 */
	public final static String POSTSCRIPT_PREFIX = "postscript";

	public final static String POSTSCRIPT_URI = "http://preservation.naa.gov.au/postScript/1.0";

	
	public final static String POSTSCRIPT_DESCRIPTION_CONTENT = 
	    "The following data represents a Base64 encoding of a postscript file";
	public final static String EXTENSION_TAG_NAME = "extension";
	public final static String DESCRIPTION_TAG_NAME = "description";
	public final static String POSTSCRIPT_EXTENSION = "ps";
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

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
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		try {
		
			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}
			
			Type type = ((XenaInputSource) input).getType();
			if (type == null) {
				// guess the type!
				try {
					type = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType((XenaInputSource) input);
				} catch (IOException e) {
					logger.log(Level.FINER, "There was an IOException guessing the type.", e);
					type = null;
				}
				if (type == null) {
					type = new UnknownType();
				}
			}
		
			ContentHandler ch = getContentHandler();
			if (!type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(PostscriptFileType.class))) {
				throw new SAXException("Postscript Normaliser - now sure about the type");
			}
			
			//Time to start normalising!
			AttributesImpl att = new AttributesImpl();
			att.addAttribute(POSTSCRIPT_URI, DESCRIPTION_TAG_NAME, DESCRIPTION_TAG_NAME, "CDATA", POSTSCRIPT_DESCRIPTION_CONTENT);
			att.addAttribute(POSTSCRIPT_URI, EXTENSION_TAG_NAME, EXTENSION_TAG_NAME, "CDATA", POSTSCRIPT_EXTENSION);
			
			InputStream is = input.getByteStream();
			ch.startElement(POSTSCRIPT_URI, POSTSCRIPT_PREFIX, POSTSCRIPT_PREFIX + ":" + POSTSCRIPT_PREFIX, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(POSTSCRIPT_URI, POSTSCRIPT_PREFIX, POSTSCRIPT_PREFIX + ":" + POSTSCRIPT_PREFIX);
		}
		catch (XenaException ex){
			throw new SAXException(ex);
		}
		
	}
}
