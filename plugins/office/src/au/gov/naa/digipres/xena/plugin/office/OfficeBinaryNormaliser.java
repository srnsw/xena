/**
 * This file is part of office.
 * 
 * office is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * office is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with office; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * This normaliser is used to normalise files that are received of type ODF. As this is one of
 * our target formats we will just be binary normalising it, so we will use this simple process
 * rather than using the main OfficeNormaliser which would first pass it through OpenOffice.
 * 
 * @author Justin Waddell
 * @author Jeff Stiff
 *
 */
public class OfficeBinaryNormaliser extends AbstractNormaliser {

	public final static String OPEN_DOCUMENT_PREFIX = "opendocument";
	private final static String OPEN_DOCUMENT_URI = "http://preservation.naa.gov.au/odf/1.0";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "Binary Office";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		ContentHandler ch = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		InputStream is = input.getByteStream();
		ch.startElement(OPEN_DOCUMENT_URI, OPEN_DOCUMENT_PREFIX, OPEN_DOCUMENT_PREFIX + ":" + OPEN_DOCUMENT_PREFIX, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(OPEN_DOCUMENT_URI, OPEN_DOCUMENT_PREFIX, OPEN_DOCUMENT_PREFIX + ":" + OPEN_DOCUMENT_PREFIX);

		// As we are just binary normalising this file the exported checksum is the same as the original file.
		String checksum = generateChecksum(input.getByteStream());
		setExportedChecksum(checksum);
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public String getOutputFileExtension() {
		return "xena";
	}

	@Override
	public boolean isConvertible() {
		return false;
	}

}
