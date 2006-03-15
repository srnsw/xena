package au.gov.naa.digipres.xena.plugin.project;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

import com.tapsterrock.mpp.MPPFile;
import com.tapsterrock.mpx.MPXException;
import com.tapsterrock.mpx.MPXFile;
import com.tapsterrock.mspdi.MSPDIFile;

/**
 * Normalise Ms Project files to Xena Project files. For pragmatic reasons, the
 * Xena project format is identical to the MS Project XML export format. This is
 * perhaps not ideal, but it would be difficult to design another format.
 *
 * @author Chris Bitmead
 */
public class MsProjectToXenaProjectNormaliser extends AbstractNormaliser {
	public String getName() {
		return "Microsoft Project";
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException {
		MPPFile mpp = null;
		try {
			mpp = new MPPFile(input.getByteStream());
		} catch (MPXException x) {
			throw new SAXException(x);
		}
		MPXFile result = new MSPDIFile(mpp);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		result.write(baos);
		XenaInputSource is = new ByteArrayInputSource(baos.toByteArray(), null);
		XMLReader reader = null;
		try {
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		} catch (ParserConfigurationException x) {
			throw new SAXException(x);
		}
		XMLFilterImpl filter = new XMLFilterImpl(reader) {
			// Without this our doc gets meta-data wrapped twice.
			public void endDocument() throws SAXException {
			}

			public void startDocument() throws SAXException {
			}
		};
		filter.setContentHandler(getContentHandler());
		reader.setContentHandler(filter);
		reader.parse(is);
	}
}
