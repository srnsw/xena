package au.gov.naa.digipres.xena.plugin.dataset;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * Convert a collection of Xena dataset files into a Xena database instance.
 *
 * @author Chris Bitmead
 */
public class MultiDatasetToXenaDatabaseNormaliser extends AbstractNormaliser {
	final static String URI = "http://preservation.naa.gov.au/database/1.0";

	final static String PREFIX = "database";

	public String getName() {
		return "Multi Dataset";
	}

	public void parse(InputSource input) throws java.io.IOException, org.xml.sax.SAXException {
		MultiInputSource minput = (MultiInputSource)input;
		ContentHandler ch = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		ch.startElement(URI, "database", PREFIX + ":" + "database", att);
		for (int i = 0; i < minput.size(); i++) {
			try {
				NormaliserManager.singleton().unwrapFragment(minput.getSystemId(i), ch);
			} catch (XenaException x) {
				throw new SAXException(x);
			} catch (ParserConfigurationException x) {
				throw new SAXException(x);
			}
		}
		ch.endElement(URI, "database", PREFIX + ":" + "database");
	}
}
