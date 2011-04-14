package au.gov.naa.digipres.xena.plugin.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadata.AbstractMetaData;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;

public class ExiftoolMetaData extends AbstractMetaData {

	public static final String EXIFTOOL_METADATA_NAME = "ExifTool Meta Data";

	// Tags
	public static final String EXIF_METADATA_TAG = "exiftool";
	public static final String METADATA_EXIF_METADATA_TAG = TagNames.METADATA_PREFIX + ":" + EXIF_METADATA_TAG;
	public static final String METADATA_URI_SUBITEM = TagNames.METADATA_URI;

	// using \u0000 <- use four digits 
	public static final String NAME_START_CHARS =
	    "a-zA-Z_\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\u10000-\\uEFFFF";
	public static final String NAME_REST_CHARS = NAME_START_CHARS + "\\-\\.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
	public static final String FULL_NAME_REGEX = "^[" + NAME_START_CHARS + "][" + NAME_REST_CHARS + "]*";

	public static final String VALID_XML_TAG_REGEX = FULL_NAME_REGEX;

	public ExiftoolMetaData() {
		super();

		name = EXIFTOOL_METADATA_NAME;
	}

	private boolean validXmlTag(String tag) {
		return tag.matches(VALID_XML_TAG_REGEX);
	}

	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		ContentHandler handler = getContentHandler();
		AttributesImpl att = new AttributesImpl();

		// Get the ExifTool executable path
		PluginManager pluginManager = metaDataManager.getPluginManager();
		PropertiesManager propManager = pluginManager.getPropertiesManager();
		String exifToolProg = propManager.getPropertyValue(MetadataProperties.METADATA_PLUGIN_NAME, MetadataProperties.EXIFTOOL_LOCATION_PROP_NAME);

		// Check that we have a valid location for the ExifTool executable
		if (exifToolProg == null || exifToolProg.equals("")) {
			throw new IOException("Cannot find the ExifTool executable. Please check its location in the metadata plugin settings.");
		}

		// Get the file to run exif tool on
		XenaInputSource xis;

		// has a file out on disk for exifTool to parse. 
		boolean hasFile = true;
		try {
			xis = (XenaInputSource) input;
			if (xis.getFile() == null) {
				hasFile = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		File fileToCheck;
		if (hasFile) {
			fileToCheck = xis.getFile();
		} else {
			fileToCheck = File.createTempFile("exiftool", ".tmp");
			FileOutputStream out = new FileOutputStream(fileToCheck);
			InputStream inputStream = xis.getByteStream();
			byte[] buff = new byte[2048];
			while (inputStream.read(buff) != -1) {
				out.write(buff);
			}
			inputStream.close();
			out.flush();
			out.close();
		}

		// Now we have the file and the exiftool prog lets use it. 
		List<String> commandList = new ArrayList<String>();
		commandList.add(exifToolProg);
		commandList.add(fileToCheck.getAbsolutePath());
		String[] commandArr = commandList.toArray(new String[0]);

		Process pr;
		pr = Runtime.getRuntime().exec(commandArr);
		BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		handler.startElement(TagNames.METADATA_URI, EXIF_METADATA_TAG, METADATA_EXIF_METADATA_TAG, att);

		String line;
		while ((line = reader.readLine()) != null) {
			// Write out to the file

			// Turn the line into a tag, data pair. 
			String[] itemArr = line.split(":", 2);
			if (itemArr.length == 1) {
				continue;
			}

			String tag = itemArr[0].trim();
			String data = itemArr[1].trim();

			/* XML tag names must start with [a-zA-Z_] then can contain [a-zA-Z0-9_-.]* (This is overly simplified, there is also unicode
			 * see the REGEX we use) but cannot contain spaces. See the XML specification ( http://www.w3.org/TR/REC-xml/ ).
			 * so we turn the tika metadata name into something more XML friendly. ':' are supported but only as namespace declarations.
			 * We will simply turn spaces and colons into '_' and if an invalid character is used as the first character then we will 
			 * prepend a '_' to the name. 
			 */
			String xmlFriendlyName = tag.replaceAll(" ", "_");
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

			if (data == null) {
				data = "";
			}

			// Replace any null (actually Hex 0x00) characters into spaces as this breaks XML and tika just lets them pass into our data.
			char i = 0x00;
			if (data.contains(Character.toString(i))) {
				data = data.replace(i, ' ');
			}

			handler.startElement(METADATA_URI_SUBITEM, xmlFriendlyName, EXIF_METADATA_TAG + ":" + xmlFriendlyName, att);
			handler.characters(data.toCharArray(), 0, data.toCharArray().length);
			handler.endElement(METADATA_URI_SUBITEM, xmlFriendlyName, EXIF_METADATA_TAG + ":" + xmlFriendlyName);

			System.out.println(tag + "(" + xmlFriendlyName + "): " + data);
		}
		reader.close();

		handler.endElement(TagNames.METADATA_URI, EXIF_METADATA_TAG, METADATA_EXIF_METADATA_TAG);

	}

	@Override
	public String getTag() {
		return METADATA_EXIF_METADATA_TAG;
	}
}
