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
	final static String PNG_PREFIX = "png";

	final static String PNG_URI = "http://preservation.naa.gov.au/png/1.0";

	final static String JPEG_PREFIX = "jpeg";

	final static String JPEG_URI = "http://preservation.naa.gov.au/jpeg/1.0";

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
			ContentHandler ch = getContentHandler();
			if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(PngFileType.class))) {
				uri = PNG_URI;
				prefix = PNG_PREFIX;
			} else if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(JpegFileType.class))) {
				uri = JPEG_URI;
				prefix = JPEG_PREFIX;
			} else {
				throw new SAXException("Image Normaliser - not sure about the type");
			}
			AttributesImpl att = new AttributesImpl();
			InputStream is = input.getByteStream();
			ch.startElement(uri, prefix, prefix + ":" + prefix, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(uri, prefix, prefix + ":" + prefix);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
