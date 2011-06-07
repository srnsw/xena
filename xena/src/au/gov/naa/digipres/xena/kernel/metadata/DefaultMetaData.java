package au.gov.naa.digipres.xena.kernel.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;
import au.gov.naa.digipres.xena.util.MetadataExtraction;

public class DefaultMetaData extends AbstractMetaData {

	public static final String DEFAULT_METADATA_NAME = "Default Meta Data";

	public static final String METADATA_URI = TagNames.METADATA_URI;
	public static final String METADATA_URI_SUBITEM = METADATA_URI;
	public static final String METADATA_TAG = TagNames.METADATA_PREFIX;
	public static final String PACKAGE_CHECKSUM_TAG = "package_checksum";
	public static final String EXPORTED_CHECKSUM_TAG = "exported_checksum";
	public static final String COMMENTS_TAG = "comments";
	public static final String TIKA_TAG = "tika";
	public static final String METADATA_QTAG = METADATA_TAG + ":" + METADATA_TAG;
	public static final String METADATA_PACKAGE_CHECKSUM = METADATA_TAG + ":" + PACKAGE_CHECKSUM_TAG;
	public static final String METADATA_EXPORTED_CHECKSUM = METADATA_TAG + ":" + EXPORTED_CHECKSUM_TAG;
	public static final String METADATA_TIKA_TAG = METADATA_TAG + ":" + TIKA_TAG;

	// using \u0000 <- use four digits 
	public static final String NAME_START_CHARS =
	    "a-zA-Z_\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\u10000-\\uEFFFF";
	public static final String NAME_REST_CHARS = NAME_START_CHARS + "\\-\\.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
	public static final String FULL_NAME_REGEX = "^[" + NAME_START_CHARS + "][" + NAME_REST_CHARS + "]*";

	public static final String VALID_XML_TAG_REGEX = FULL_NAME_REGEX;

	private String description = "This checksum is created from the entire contents of the " + TagNames.PACKAGE_CONTENT
	                             + " tag, not including the tag itself";

	private String exportedDescription = "This is the checksum of the exported file.";

	public DefaultMetaData() {
		super();

		name = DEFAULT_METADATA_NAME;
	}

	private boolean validXmlTag(String tag) {
		return tag.matches(VALID_XML_TAG_REGEX);
	}

	private void useMetadataExtractionTool(InputSource input) throws SAXException {

		// test the new Tikka util. 
		XenaInputSource xis = (XenaInputSource) input;
		Map<String, String> metadata = MetadataExtraction.extractMetadataWithTika(xis);

		Set<String> names = metadata.keySet();
		ArrayList<String> keys = new ArrayList<String>(names);
		Collections.sort(keys);

		AttributesImpl atts = new AttributesImpl();
		ContentHandler handler = getContentHandler();

		try {
			handler.startElement(METADATA_URI, TIKA_TAG, METADATA_TIKA_TAG, atts);
			for (String key : keys) {

				if (key == null) {
					continue;
				}

				/* XML tag names must start with [a-zA-Z_] then can contain [a-zA-Z0-9_-.]* (This is overly simplified, there is also unicode
				 * see the REGEX we use) but cannot contain spaces. See the XML specification ( http://www.w3.org/TR/REC-xml/ ).
				 * so we turn the tika metadata name into something more XML friendly. ':' are supported but only as namespace declarations.
				 * We will simply turn spaces and colons into '_' and if an invalid character is used as the first character then we will 
				 * prepend a '_' to the name. 
				 */
				String xmlFriendlyName = key.replaceAll(" ", "_");
				xmlFriendlyName = xmlFriendlyName.replaceAll(":", "_");
				xmlFriendlyName = xmlFriendlyName.replaceAll("/", "");
				if (!xmlFriendlyName.matches("^[" + NAME_START_CHARS + "].*")) {
					xmlFriendlyName = "_" + xmlFriendlyName;
				}

				// Check to see if it's a valid XML tag based on the VALID_XML_TAG_REGEX regex. If not then log it and skip the "tag".
				if (!validXmlTag(xmlFriendlyName)) {
					logger.warning(xmlFriendlyName + " is not a valid XML tag! it doesn't match the form: " + VALID_XML_TAG_REGEX);
					continue;
				}

				String data = metadata.get(key);
				if (data == null) {
					data = "";
				}

				// Replace any null (actually Hex 0x00) characters into spaces as this breaks XML and tika just lets them pass into our data.
				char i = 0x00;
				if (data.contains(Character.toString(i))) {
					data = data.replace(i, ' ');
				}

				handler.startElement(METADATA_URI_SUBITEM, xmlFriendlyName, TIKA_TAG + ":" + xmlFriendlyName, atts);
				handler.characters(data.toCharArray(), 0, data.toCharArray().length);
				handler.endElement(METADATA_URI_SUBITEM, xmlFriendlyName, TIKA_TAG + ":" + xmlFriendlyName);

			}
		} catch (SAXException ex) {
			throw ex;
		} finally {
			handler.endElement(METADATA_URI, TIKA_TAG, METADATA_TIKA_TAG);
		}

	}

	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		ContentHandler handler = getContentHandler();

		AttributesImpl att = new AttributesImpl();
		handler.startElement(METADATA_URI, METADATA_TAG, METADATA_QTAG, att);

		// Add the Xena file checksum.
		String digest = (String) getProperty("http://xena/digest");
		if (digest == null) {
			digest = "";
		}

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
		String exportedDigestComment = (String) getProperty("http://xena/exported_digest_comment");

		if (exportedDigest == null) {
			exportedDigest = "";
		}

		// Add the checksum element
		if (digest != null) {
			AttributesImpl atts = new AttributesImpl();
			handler.startElement(METADATA_URI, EXPORTED_CHECKSUM_TAG, METADATA_EXPORTED_CHECKSUM, atts);

			atts.addAttribute(METADATA_URI, "description", METADATA_TAG + ":description", "CDATA", exportedDescription);
			atts.addAttribute(METADATA_URI, "algorithm", METADATA_TAG + ":algorithm", "CDATA", TagNames.DEFAULT_CHECKSUM_ALGORITHM);
			handler.startElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE, atts);
			handler.characters(exportedDigest.toCharArray(), 0, exportedDigest.toCharArray().length);
			handler.endElement(METADATA_URI, TagNames.SIGNATURE, METADATA_TAG + ":" + TagNames.SIGNATURE);

			// Add any comments made by the normaliser... if there were any.
			if (exportedDigestComment != null) {
				atts = new AttributesImpl();
				handler.startElement(METADATA_URI, COMMENTS_TAG, METADATA_TAG + ":" + COMMENTS_TAG, atts);
				handler.characters(exportedDigestComment.toCharArray(), 0, exportedDigestComment.toCharArray().length);
				handler.endElement(METADATA_URI, COMMENTS_TAG, METADATA_TAG + ":" + COMMENTS_TAG);
			}

			handler.endElement(METADATA_URI, EXPORTED_CHECKSUM_TAG, METADATA_EXPORTED_CHECKSUM);
		}

		useMetadataExtractionTool(input);

		handler.endElement(METADATA_URI, METADATA_TAG, METADATA_QTAG);
	}

	@Override
	public String getTag() {
		return METADATA_QTAG;
	}

}
