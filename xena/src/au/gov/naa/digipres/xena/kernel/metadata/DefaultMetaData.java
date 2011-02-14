package au.gov.naa.digipres.xena.kernel.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;

public class DefaultMetaData extends AbstractMetaData {

	public static final String DEFAULT_METADATA_NAME = "Default Meta Data";

	public static final String METADATA_URI = TagNames.METADATA_URI;
	public static final String METADATA_TAG = TagNames.METADATA_PREFIX;
	public static final String PACKAGE_CHECKSUM_TAG = "package_checksum";
	public static final String EXPORTED_CHECKSUM_TAG = "exported_checksum";
	public static final String TIKA_TAG = "tika";
	public static final String METADATA_QTAG = METADATA_TAG + ":" + METADATA_TAG;
	public static final String METADATA_PACKAGE_CHECKSUM = METADATA_TAG + ":" + PACKAGE_CHECKSUM_TAG;
	public static final String METADATA_EXPORTED_CHECKSUM = METADATA_TAG + ":" + EXPORTED_CHECKSUM_TAG;
	public static final String METADATA_TIKA_TAG = METADATA_TAG + ":" + TIKA_TAG;
	public static final String VALID_XML_TAG_REGEX = "^[a-zA-Z_][a-zA-Z0-9_\\-\\.]*";

	private String description = "This checksum is created from the entire contents of the " + TagNames.PACKAGE_CONTENT
	                             + " tag, not including the tag itself";

	private String exportedDescription = "This is the checksum of the exported file.";

	//Tika variables
	private ParseContext context;
	private Detector detector;
	private AutoDetectParser parser;
	private Metadata metadata;

	public DefaultMetaData() {
		super();

		name = DEFAULT_METADATA_NAME;
	}

	private boolean validXmlTag(String tag) {
		return tag.matches(VALID_XML_TAG_REGEX);
	}

	private ContentHandler getTikaContentHandler() throws SAXException {
		return new DefaultHandler() {
			public void endDocument() throws SAXException {
				String[] names = metadata.names();
				Arrays.sort(names);

				AttributesImpl atts = new AttributesImpl();
				ContentHandler handler = getContentHandler();

				handler.startElement(METADATA_URI, TIKA_TAG, METADATA_TIKA_TAG, atts);

				for (String name : names) {

					/* XML tag names must start with [a-zA-Z_] then can contain [a-zA-Z0-9_-.]* but cannot contain spaces  
					 * so we turn the tika metadata name into something more XML friendly. ':' are supported but only as namespace declarations.
					 * We will simply turn spaces and colons into '_' and if an invalid character is used as the first character then we will 
					 * prepend a '_' to the name. 
					 */
					String xmlFriendlyName = name.replaceAll(" ", "_");
					xmlFriendlyName = xmlFriendlyName.replaceAll(":", "_");
					xmlFriendlyName = xmlFriendlyName.replaceAll("/", "");
					if (!xmlFriendlyName.matches("^[a-zA-Z_].*")) {
						xmlFriendlyName = "_" + xmlFriendlyName;
					}

					// Check to see if it's a valid XML tag based on the VALID_XML_TAG_REGEX regex. If not then log it and skip the "tag".
					if (!validXmlTag(xmlFriendlyName)) {
						logger.warning(xmlFriendlyName + " is not a valid XML tag.. doesn't match the form: " + VALID_XML_TAG_REGEX);
						continue;
					}

					handler.startElement(METADATA_URI, xmlFriendlyName, TIKA_TAG + ":" + xmlFriendlyName, atts);
					handler.characters(metadata.get(name).toCharArray(), 0, metadata.get(name).toCharArray().length);
					handler.endElement(METADATA_URI, xmlFriendlyName, TIKA_TAG + ":" + xmlFriendlyName);

					System.out.println(name + "(" + xmlFriendlyName + "): " + metadata.get(name));
				}

				handler.endElement(METADATA_URI, TIKA_TAG, METADATA_TIKA_TAG);

			}

		};

	}

	private void useMetadataExtractionTool() {
		InputSource input;
		XenaInputSource xis;

		// has a file out on disk for tika to parse. 
		boolean hasFile = true;
		try {
			input = (InputSource) this.getProperty("http://xena/input");
			xis = (XenaInputSource) input;
			if (xis.getFile() == null) {
				hasFile = false;
			}
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			File tmpFile = null;

			context = new ParseContext();
			detector = (new TikaConfig()).getMimeRepository();
			parser = new AutoDetectParser(detector);
			context.set(Parser.class, parser);

			metadata = new Metadata();
			InputStream inputStream;

			if (hasFile) {
				URL url = new URL(input.getSystemId());
				inputStream = TikaInputStream.get(xis.getFile(), metadata);
			} else {
				tmpFile = File.createTempFile("tiki", null);
				FileOutputStream out = new FileOutputStream(tmpFile);
				inputStream = ((XenaInputSource) input).getByteStream();
				byte[] buff = new byte[2048];
				while (inputStream.read(buff) != -1) {
					out.write(buff);
				}
				inputStream.close();
				out.flush();
				out.close();

				inputStream = TikaInputStream.get(tmpFile.toURI(), metadata);
			}
			parser.parse(inputStream, getTikaContentHandler(), metadata, context);

			if ((tmpFile != null) && (tmpFile.exists())) {
				tmpFile.delete();
			}

		} catch (MimeTypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		//		Tika tika = new Tika();
		//		Metadata meta = new Metadata();
		//		try {
		//			System.out.println(tika.detect(input.getByteStream(), meta));
		//			System.out.println(meta.toString());
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
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

			handler.endElement(METADATA_URI, EXPORTED_CHECKSUM_TAG, METADATA_EXPORTED_CHECKSUM);
		}

		useMetadataExtractionTool();

		handler.endElement(METADATA_URI, METADATA_TAG, METADATA_QTAG);
	}

}
