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

package au.gov.naa.digipres.xena.plugin.pdf;

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
 * Convert PDF to the Xena PDF format.
 *
 */
public class PdfToXenaPdfNormaliser extends AbstractNormaliser {
	public final static String PDF_PREFIX = "pdf";

	final static String PDF_URI = "http://preservation.naa.gov.au/pdf/1.0";

	@Override
    public String getName() {
		return "PDF";
	}

	@Override
    public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		ContentHandler ch = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		InputStream is = input.getByteStream();
		ch.startElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX);
	}
}
