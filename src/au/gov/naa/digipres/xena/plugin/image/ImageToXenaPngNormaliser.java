package au.gov.naa.digipres.xena.plugin.image;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFDirectory;

/**
 * Normaliser for Java supported image types other than the core JPEG and PNG.
 * Will convert into PNG.
 *
 * @author Chris Bitmead
 */
public class ImageToXenaPngNormaliser extends AbstractNormaliser {
	final static String PREFIX = "png";

	final static String URI = "http://preservation.naa.gov.au/png/1.0";

	final static String MPREFIX = "multipage";

	final static String MURI = "http://preservation.naa.gov.au/multipage/1.0";

	public ImageToXenaPngNormaliser() {
	}

	public String getName() {
		return "Image";
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException {
		SeekableStream ss = new FileCacheSeekableStream(input.getByteStream());
		RenderedOp src = JAI.create("Stream", ss);

		Object td = src.getProperty("tiff_directory");
		if (td instanceof TIFFDirectory) {
			ParameterBlock pb = new ParameterBlock();
			pb.add(ss);
			TIFFDecodeParam param = new TIFFDecodeParam();
			pb.add(param);

			int numImages = 0;
			long nextOffset = 0;
			List<RenderedOp> images = new ArrayList<RenderedOp>();
			do {
				src = JAI.create("tiff", pb);
				images.add(src);
				TIFFDirectory dir = (TIFFDirectory)src.getProperty("tiff_directory");
				nextOffset = dir.getNextIFDOffset();
				if (nextOffset != 0) {
					param.setIFDOffset(nextOffset);
				}
				numImages++;
			} while (nextOffset != 0);

			if (1 < numImages) {
				param.setIFDOffset(nextOffset);
				ContentHandler ch = getContentHandler();
				AttributesImpl att = new AttributesImpl();
				ch.startElement(MURI, "multipage", MPREFIX + ":multipage", att);
				
				for (RenderedOp image : images)
				{
					ch.startElement(MURI, "page", MPREFIX + ":page", att);
					outputImage(image);
					ch.endElement(MURI, "page", MPREFIX + ":page");
				}
				ch.endElement(URI, "multipage", MPREFIX + ":multipage");
			} else {
				outputImage(src);
			}
		} else {
			outputImage(src);
		}
	}

	void outputImage(RenderedOp src) throws SAXException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RenderedOp imageOp;
		try {
			// Encode the file as a PNG image.
			imageOp = JAI.create("encode", src, baos, "PNG", null);
		} catch (Exception x) {
			// For some reason JAI can throw RuntimeExceptions on bad data.
			throw new SAXException(x);
		}
		AttributesImpl att = new AttributesImpl();
		ContentHandler ch = getContentHandler();
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		ch.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);
		imageOp.dispose();
		src.dispose();
		baos.close();
		is.close();
	}
}
