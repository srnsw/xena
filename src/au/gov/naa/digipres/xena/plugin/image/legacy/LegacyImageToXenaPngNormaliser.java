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

package au.gov.naa.digipres.xena.plugin.image.legacy;

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
import au.gov.naa.digipres.xena.plugin.image.BasicImageNormaliser;
import au.gov.naa.digipres.xena.plugin.image.ReleaseInfo;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.JimiReader;

/**
 * Normaliser for Java supported image types other than the core JPEG and PNG.
 * Will convert into PNG.
 *
 */
public class LegacyImageToXenaPngNormaliser extends AbstractNormaliser {
	final static String IMG_PREFIX = "png";

	private final static String PNG_MIME_TYPE = "image/png";

	final static String URI = "http://preservation.naa.gov.au/png/1.0";

	final static String MPREFIX = "multipage";

	final static String MURI = "http://preservation.naa.gov.au/multipage/1.0";

	public LegacyImageToXenaPngNormaliser() {
	}

	@Override
	public String getName() {
		return "Legacy Image";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		JimiReader reader;
		try {
			reader = Jimi.createJimiReader(input.getByteStream());
		} catch (JimiException e) {
			throw new SAXException(e);
		}

		Enumeration imageEnum = reader.getImageEnumeration();
		List<Image> imageList = new ArrayList<Image>();

		while (imageEnum.hasMoreElements()) {
			imageList.add((Image) imageEnum.nextElement());
		}

		if (imageList.isEmpty()) {
			throw new IOException("Parsing failed - invalid image file");
		} else if (imageList.size() == 1) {
			outputImage(imageList.get(0));
		} else {
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(MURI, "multipage", MPREFIX + ":multipage", att);

			for (Image image : imageList) {
				ch.startElement(MURI, "page", MPREFIX + ":page", att);
				outputImage(image);
				ch.endElement(MURI, "page", MPREFIX + ":page");
			}

			ch.endElement(URI, "multipage", MPREFIX + ":multipage");
		}
		reader.close();
	}

	private void outputImage(Image image) throws SAXException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Jimi.putImage(PNG_MIME_TYPE, image, baos);
		} catch (JimiException e) {
			throw new SAXException(e);
		}

		AttributesImpl att = new AttributesImpl();

		att
		        .addAttribute(URI, BasicImageNormaliser.PNG_PREFIX, BasicImageNormaliser.PNG_PREFIX, "CDATA",
		                      BasicImageNormaliser.PNG_DESCRIPTION_CONTENT);

		ContentHandler ch = getContentHandler();
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		ch.startElement(URI, IMG_PREFIX, IMG_PREFIX + ":" + IMG_PREFIX, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(URI, IMG_PREFIX, IMG_PREFIX + ":" + IMG_PREFIX);
		is.close();
		baos.close();
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
