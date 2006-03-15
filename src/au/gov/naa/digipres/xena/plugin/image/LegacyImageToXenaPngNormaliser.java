package au.gov.naa.digipres.xena.plugin.image;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiReader;

/**
 * Normaliser for Java supported image types other than the core JPEG and PNG.
 * Will convert into PNG.
 *
 * @author Chris Bitmead
 */
public class LegacyImageToXenaPngNormaliser extends AbstractNormaliser {
	final static String IMG_PREFIX = "png";
	
	private final static String PNG_MIME_TYPE = "image/png";

	final static String URI = "http://preservation.naa.gov.au/png/1.0";

	final static String MPREFIX = "multipage";

	final static String MURI = "http://preservation.naa.gov.au/multipage/1.0";

	public LegacyImageToXenaPngNormaliser() {
	}

	public String getName() {
		return "Legacy Image";
	}
	
	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException
	{
		JimiReader reader;
		try
		{
			reader = Jimi.createJimiReader(input.getByteStream());
		}
		catch (JimiException e)
		{
			throw new SAXException(e);
		}
		
		Enumeration imageEnum = reader.getImageEnumeration();
		List<Image> imageList = new ArrayList<Image>();
		
		while (imageEnum.hasMoreElements())
		{
			imageList.add((Image)imageEnum.nextElement());
		}
		
		if (imageList.isEmpty())
		{
			throw new IOException("Parsing failed - invalid image file");
		}
		else if (imageList.size() == 1)
		{
			outputImage(imageList.get(0));
		}
		else
		{
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(MURI, "multipage", MPREFIX + ":multipage", att);
			
			for (Image image : imageList)
			{
				ch.startElement(MURI, "page", MPREFIX + ":page", att);
				outputImage(image);
				ch.endElement(MURI, "page", MPREFIX + ":page");
			}
		
			ch.endElement(URI, "multipage", MPREFIX + ":multipage");
		}
		reader.close();
	}

	private void outputImage(Image image) throws SAXException, IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			Jimi.putImage(PNG_MIME_TYPE, image, baos);
		}
		catch (JimiException e)
		{
			throw new SAXException(e);
		}

		AttributesImpl att = new AttributesImpl();
		ContentHandler ch = getContentHandler();
		ch.startElement(URI, IMG_PREFIX, IMG_PREFIX + ":" + IMG_PREFIX, att);
		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		// 80 characters makes nice looking output
		byte[] buf = new byte[BasicImageNormaliser.CHUNK_SIZE];
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
		ch.endElement(URI, IMG_PREFIX, IMG_PREFIX + ":" + IMG_PREFIX);
	}
}
