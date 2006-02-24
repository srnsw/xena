package au.gov.naa.digipres.xena.plugin.naa;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import com.Ostermiller.util.MD5;

/**
 * Add an outer meta-data wrapper around the data containing the MD5 checksum.
 * It has to be a second meta-data layer so that the core meta-data is
 * incorporated into the checksum.
 *
 * @author Chris Bitmead
 */
public class NaaOuterWrapNormaliser extends XMLFilterImpl {
	final static String CHECKSUM_PREFIX = "checksum";

	final static String CHECKSUM_URI = "http://preservation.naa.gov.au/checksum/1.0";

	MD5 md5;

	public void setMD5(MD5 md5) {
		this.md5 = md5;
	}

	public String toString() {
		return "NAA Package Wrap Outer";
	}

	public void startDocument() throws org.xml.sax.SAXException {
		super.startDocument();
		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.startElement(NaaTagNames.PACKAGE_URI, "package", "package:package", att);
		th.startElement(NaaTagNames.PACKAGE_URI, "content", "package:content", att);
	}

	public void endDocument() throws org.xml.sax.SAXException {
		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.endElement(NaaTagNames.PACKAGE_URI, "content", "package:content");
		th.startElement(NaaTagNames.PACKAGE_URI, "meta", "package:meta", att);
		th.startElement(CHECKSUM_URI, "checksum","checksum:checksum", att);
		String smd5 = md5.getHashString();
		th.characters(smd5.toCharArray(), 0, smd5.length());
		th.endElement(CHECKSUM_URI, "checksum", "checksum:checksum");
		th.endElement(NaaTagNames.PACKAGE_URI, "meta", "package:meta");
		th.endElement(NaaTagNames.PACKAGE_URI, "package","package:package");
		super.endDocument();
	}
}
