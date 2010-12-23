package au.gov.naa.digipres.xena.kernel.metadata;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;

public class DefaultMetaData extends AbstractMetaData {

	public static final String DEFAULT_METADATA_NAME = "Default Meta Data";

	public static final String METADATA_URI = TagNames.METADATA_URI;
	public static final String METADATA_TAG = TagNames.METADATA_PREFIX;
	public static final String PACKAGE_CHECKSUM_TAG = "package_checksum";
	public static final String EXPORTED_CHECKSUM_TAG = "exported_checksum";
	public static final String METADATA_QTAG = METADATA_TAG + ":" + METADATA_TAG;
	public static final String METADATA_PACKAGE_CHECKSUM = METADATA_TAG + ":" + PACKAGE_CHECKSUM_TAG;
	public static final String METADATA_EXPORTED_CHECKSUM = METADATA_TAG + ":" + EXPORTED_CHECKSUM_TAG;

	private String description = "This checksum is created from the entire contents of the " + TagNames.PACKAGE_CONTENT
	                             + " tag, not including the tag itself";

	private String exportedDescription = "This is the checksum of the exported file.";

	public DefaultMetaData() {
		super();

		name = DEFAULT_METADATA_NAME;
	}

	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		ContentHandler handler = getContentHandler();

		AttributesImpl att = new AttributesImpl();
		handler.startElement(METADATA_URI, METADATA_TAG, METADATA_QTAG, att);

		// Add the Xena file checksum.
		String digest = (String) getProperty("http://xena/digest");
		// Add the checksum element
		if (digest != null) {
			AttributesImpl atts = new AttributesImpl();
			handler.startElement(METADATA_URI, PACKAGE_CHECKSUM_TAG, METADATA_PACKAGE_CHECKSUM, atts);

			atts.addAttribute(METADATA_URI, "description", METADATA_TAG + ":description", "CDATA", description);
			atts.addAttribute(METADATA_URI, "algorithm", METADATA_TAG + ":algorithm", "CDATA", TagNames.DEFAULT_CHECKSUM_ALGORITHM);
			handler.startElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE, atts);
			handler.characters(digest.toCharArray(), 0, digest.toCharArray().length);
			handler.endElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE);

			handler.endElement(METADATA_URI, PACKAGE_CHECKSUM_TAG, METADATA_PACKAGE_CHECKSUM);
		}

		// Add the exported file checksum.
		String exportedDigest = (String) getProperty("http://xena/exported_digest");
		// Add the checksum element
		if (digest != null) {
			AttributesImpl atts = new AttributesImpl();
			handler.startElement(METADATA_URI, EXPORTED_CHECKSUM_TAG, METADATA_EXPORTED_CHECKSUM, atts);

			atts.addAttribute(METADATA_URI, "description", METADATA_TAG + ":description", "CDATA", exportedDescription);
			atts.addAttribute(METADATA_URI, "algorithm", METADATA_TAG + ":algorithm", "CDATA", TagNames.DEFAULT_CHECKSUM_ALGORITHM);
			handler.startElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE, atts);
			handler.characters(exportedDigest.toCharArray(), 0, exportedDigest.toCharArray().length);
			handler.endElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE);

			handler.endElement(METADATA_URI, EXPORTED_CHECKSUM_TAG, METADATA_EXPORTED_CHECKSUM);
		}

		handler.endElement(METADATA_URI, METADATA_TAG, METADATA_QTAG);
	}

}
