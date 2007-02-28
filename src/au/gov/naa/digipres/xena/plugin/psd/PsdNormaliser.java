package au.gov.naa.digipres.xena.plugin.psd;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.sanselan.ImageReadException;
import org.sanselan.Sanselan;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Normaliser for PSD image types.
 * Will convert into PNG.
 *   
 * the PSD Normaliser extends from AbstractNormaliser         
 * @see au.gov.naa.digipres.xena.kernel.guesser 
 * 
 * @author Kamaj Jayakantha de Mel
 * @since 09-Feb-2007
 * 
 * @version 1.2
 * 
 */
public class PsdNormaliser extends AbstractNormaliser {

	/**
	 * characters per line
	 */
	public static final int MAX_LINE_LENGTH = 76;
	
	/**
	 * chunk size
	 */
	public static final int CHUNK_SIZE = (MAX_LINE_LENGTH * 3) / 4;


	final static String PNG_PREFIX = "png";

	final static String PNG_URI = "http://preservation.naa.gov.au/png/1.0";

	/**
	 * @return String: Normalizer Name
	 */
	@Override
	public String getName() {
		return "PSD";
	}

	/**
	 * create PNG Xena Normalized Image
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results)
			throws IOException, SAXException {

		BufferedImage psdBufferedImage;
		
		try {
			
			psdBufferedImage = new Sanselan().getBufferedImage(input.getByteStream());	
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		   
			RenderedOp awtImageOp = JAI.create("AWTImage", (Image)psdBufferedImage);
	        JAI.create("encode", awtImageOp, outputStream, PNG_PREFIX);
	       
	        ByteArrayInputStream inputSream = new ByteArrayInputStream(outputStream.toByteArray());
	       
	        ContentHandler contentHandler = getContentHandler();			
			
	        AttributesImpl attribute = new AttributesImpl();
			
			contentHandler.startElement(PNG_URI, PNG_PREFIX, PNG_PREFIX + ":" + PNG_PREFIX, attribute);
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();

			byte[] buffer = new byte[PsdNormaliser.CHUNK_SIZE];
			int count;
			while (0 <= (count = inputSream.read(buffer))) {
				byte[] tmpBuffer = buffer;
				if (count < buffer.length) {
					tmpBuffer = new byte[count];
					System.arraycopy(buffer, 0, tmpBuffer, 0, count);
				}
				char[] charArray = encoder.encode(tmpBuffer).toCharArray();
				contentHandler.characters(charArray, 0, charArray.length);
			}
			contentHandler.endElement(PNG_URI, PNG_PREFIX, PNG_PREFIX + ":" + PNG_PREFIX);
	        
		} catch (ImageReadException e) {
			e.printStackTrace();
		}
	}
}
