package au.gov.naa.digipres.xena.plugin.image;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Normaliser for the core Xena types of PNG and JPEG.
 *
 *
 * AAK: THIS IS VERY POORLY DONE!
 * XXX - image normaliser expects xena input source but only requires input source
 * FIXME - image normaliser expects xena input source but only requires input source
 * In the code, as at october 2005, the parse function needs significant rework.
 *  Of note, the parse excepts an InputSource, and then assumes that it is a xena input source.
 *  There is now basic error handling, however this entire code block needs to be redesigned.
 *  Perhaps all normalisers should implement parse(XenaInputSource xis)  - this would seem BETTER...
 *  
 *
 *
 * @author Chris Bitmead
 */
abstract public class BasicImageNormaliser extends AbstractNormaliser {
	public final static String PNG_PREFIX = "png";

	public final static String PNG_URI = "http://preservation.naa.gov.au/png/1.0";

	public final static String JPEG_PREFIX = "jpeg";

	public final static String JPEG_URI = "http://preservation.naa.gov.au/jpeg/1.0";

    public final static String DESCRIPTION_TAG_NAME = "description";

    public final static String JPEG_DESCRIPTION_CONTENT = "The following data represents a Base64 encoding of a JPEG image file ( ISO Standard 10918-1 )";
    
    public final static String PNG_DESCRIPTION_CONTENT = "The following data represents a Base64 encoding of a PNG image file ( ISO Standard 15948 ).";
    
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException {
		try {
            //TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this appropriately.
            // ie - this method should be parse(XenaInputSource xis)
            if (!(input instanceof XenaInputSource)) {
                throw new XenaException("Can only normalise XenaInputSource objects.");
            }
            Type type = ((XenaInputSource)input).getType();
            if (type == null) {
                // guess the type!
                try {
                    type =  normaliserManager.getPluginManager().getGuesserManager().mostLikelyType((XenaInputSource)input);
                } catch (IOException e) {
                    //sysout
                    logger.log(Level.FINER, "There was an IOException guessing the type.", e);
                    type = null;
                }
                if (type == null) {
                    type = new UnknownType();
                }
            }

            
			String prefix;
			String uri;
            String description;
            ContentHandler ch = getContentHandler();
			if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(PngFileType.class))) {
				uri = PNG_URI;
				prefix = PNG_PREFIX;
                description = PNG_DESCRIPTION_CONTENT;
			} else if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(JpegFileType.class))) {
				uri = JPEG_URI;
				prefix = JPEG_PREFIX;
                description = JPEG_DESCRIPTION_CONTENT;
			} else {
				throw new SAXException("Image Normaliser - not sure about the type");
			}
			AttributesImpl att = new AttributesImpl();
            att.addAttribute(uri, DESCRIPTION_TAG_NAME, DESCRIPTION_TAG_NAME, "CDATA", description);
			
			InputStream is = input.getByteStream();
			ch.startElement(uri, prefix, prefix + ":" + prefix, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(uri, prefix, prefix + ":" + prefix);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
