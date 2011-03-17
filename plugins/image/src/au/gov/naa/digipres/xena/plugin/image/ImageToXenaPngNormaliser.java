/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Normaliser for Java supported image types other than the core JPEG and PNG.
 * Will convert into PNG.
 *
 */
public class ImageToXenaPngNormaliser extends AbstractNormaliser {
	final static String PREFIX = BasicImageNormaliser.PNG_PREFIX;

	final static String URI = BasicImageNormaliser.PNG_URI;

	final static String MPREFIX = "multipage";

	final static String MURI = "http://preservation.naa.gov.au/multipage/1.0";

	public ImageToXenaPngNormaliser() {
		// Nothing to do
	}

	@Override
	public String getName() {
		return "Image";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		try {
			BufferedImage src = Sanselan.getBufferedImage(input.getByteStream());
			outputImage(src);
		} catch (ImageReadException x) {
			throw new SAXException(x);
		}
	}

	void outputImage(BufferedImage src) throws SAXException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// Encode the file as a PNG image.
			Sanselan.writeImage(src, baos, ImageFormat.IMAGE_FORMAT_PNG, null);
		} catch (Exception x) {
			// For some reason JAI can throw RuntimeExceptions on bad data.
			throw new SAXException(x);
		}

		AttributesImpl att = new AttributesImpl();
		att.addAttribute(URI, BasicImageNormaliser.DESCRIPTION_TAG_NAME, PREFIX + ":" + BasicImageNormaliser.DESCRIPTION_TAG_NAME, "CDATA",
		                 BasicImageNormaliser.PNG_DESCRIPTION_CONTENT);
		ContentHandler ch = getContentHandler();
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		ch.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);
		is.close();

		// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata.
		is = new ByteArrayInputStream(baos.toByteArray());
		setExportedChecksum(generateChecksum(is));

		baos.close();
		is.close();
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
