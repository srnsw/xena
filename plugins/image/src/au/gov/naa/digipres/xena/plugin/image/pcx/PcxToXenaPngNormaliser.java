/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
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
 * @author Jeff Stiff
 */

/*
 * Created on 19/04/2006 justinw5
 * 
 * NOTE: This normaliser has been replaced by the Image Magick normaliser, it probably should be deleted, but once we remove the 
 * image magick library it may need to be re-implemented, so it serves as a reference. 
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.pcx;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.plugin.image.ReleaseInfo;

@Deprecated
public class PcxToXenaPngNormaliser extends AbstractNormaliser {
	final static String PNG_PREFIX = "png";

	final static String PNG_URI = "http://preservation.naa.gov.au/png/1.0";

	/**
	 * RFC suggests max of 76 characters per line
	 */
	public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

	/**
	 * Base64 turns 3 characters into 4...
	 */
	public static final int CHUNK_SIZE = MAX_BASE64_RFC_LINE_LENGTH * 3 / 4;

	public PcxToXenaPngNormaliser() {
		super();
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		// TODO: Note that the migrateOnly flag has only been added to allow Xena/Plugins to compile cleanly, it is NOT implemented in this module.

		Image pcxImage = PcxReader.decodeImage(input.getByteStream());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		//		RenderedOp awtImageOp = JAI.create("AWTImage", pcxImage);
		//		JAI.create("encode", awtImageOp, baos, "png");

		ByteArrayInputStream byteIS = new ByteArrayInputStream(baos.toByteArray());

		ContentHandler ch = getContentHandler();

		AttributesImpl att = new AttributesImpl();
		ch.startElement(PNG_URI, PNG_PREFIX, PNG_PREFIX + ":" + PNG_PREFIX, att);

		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();

		// 80 characters makes nice looking output
		byte[] buf = new byte[CHUNK_SIZE];
		int c;
		while (0 <= (c = byteIS.read(buf))) {
			byte[] tbuf = buf;
			if (c < buf.length) {
				tbuf = new byte[c];
				System.arraycopy(buf, 0, tbuf, 0, c);
			}
			char[] chs = encoder.encode(tbuf).toCharArray();
			ch.characters(chs, 0, chs.length);
		}
		ch.endElement(PNG_URI, PNG_PREFIX, PNG_PREFIX + ":" + PNG_PREFIX);

	}

	@Override
	public String getName() {
		return "PCX";
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public boolean isConvertible() {
		return false;
	}

	@Override
	public String getOutputFileExtension() {
		return "png";
	}

}
