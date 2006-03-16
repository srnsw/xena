package au.gov.naa.digipres.xena.plugin.image;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;

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

	/**
	 * RFC suggests max of 76 characters per line
	 */
	public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

	/**
	 * Base64 turns 3 characters into 4...
	 */
	public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;

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
                System.out.println("in image normaliser, type is null.");
                // guess the type!
                try {
                    type =  normaliserManager.getPluginManager().getGuesserManager().mostLikelyType((XenaInputSource)input);
                } catch (IOException e) {
                    //sysout
                    System.out.println("There was an IOException guessing the type.");
                    e.printStackTrace(System.out);
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
			ch.startElement(uri, prefix, prefix + ":" + prefix, att);

			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			InputStream is = input.getByteStream();

			// 80 characters makes nice looking output
			byte[] buf = new byte[CHUNK_SIZE];
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
			ch.endElement(uri, prefix, prefix + ":" + prefix);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}
}
