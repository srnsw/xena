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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.image.tiff;

import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.plugin.image.BasicImageNormaliser;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.util.Rational;

/**
 * Normaliser for Java supported image types other than the core JPEG and PNG.
 * Will convert into PNG.
 *
 */
public class TiffToXenaPngNormaliser extends AbstractNormaliser {
	private final static int TIFF_TAG_SIZE = 12;
	private final static int TAG_ENTRY_TAG_ID_OFFSET = 0;
	private final static int TAG_ENTRY_DATA_TYPE_OFFSET = 2;
	private final static int TAG_ENTRY_DATA_COUNT_OFFSET = 4;
	private final static int TAG_ENTRY_DATA_OFFSET = 8;

	final static String PNG_PREFIX = BasicImageNormaliser.PNG_PREFIX;
	final static String PNG_TAG = BasicImageNormaliser.PNG_TAG;
	final static String PNG_URI = BasicImageNormaliser.PNG_URI;
	final static String XMP_TAG = "xmp";

	final static String EXIF_URI = "http://ns.adobe.com/exif/1.0";
	final static String EXIF_PREFIX = "exif";
	final static String EXIF_ROOT_TAG = "exif";
	final static String EXIF_TAG_TAG = "tag";

	final static String MULTIPAGE_PREFIX = "multipage";
	final static String MULTIPAGE_URI = "http://preservation.naa.gov.au/multipage/1.0";
	final static String METADATA_TAG = "metadata";

	public TiffToXenaPngNormaliser() {
	}

	@Override
    public String getName() {
		return "Image";
	}

	@Override
    public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		SeekableStream ss = new FileCacheSeekableStream(input.getByteStream());
		RenderedOp src = JAI.create("Stream", ss);

		Object td = src.getProperty("tiff_directory");
		if (td instanceof TIFFDirectory) {
			ParameterBlock pb = new ParameterBlock();
			pb.add(ss);
			TIFFDecodeParam param = new TIFFDecodeParam();
			pb.add(param);

			int numImages = 0;
			long nextOffset = 0;
			List<RenderedOp> images = new ArrayList<RenderedOp>();
			Map<RenderedOp, TIFFDirectory> imageToMetadataMap = new HashMap<RenderedOp, TIFFDirectory>();
			do {
				src = JAI.create("tiff", pb);
				images.add(src);
				TIFFDirectory dir = (TIFFDirectory) src.getProperty("tiff_directory");
				imageToMetadataMap.put(src, dir);

				nextOffset = dir.getNextIFDOffset();
				if (nextOffset != 0) {
					param.setIFDOffset(nextOffset);
				}
				numImages++;
			} while (nextOffset != 0);

			if (1 < numImages) {
				param.setIFDOffset(nextOffset);
				ContentHandler ch = getContentHandler();
				AttributesImpl att = new AttributesImpl();
				ch.startElement(MULTIPAGE_URI, "multipage", MULTIPAGE_PREFIX + ":multipage", att);

				for (RenderedOp image : images) {
					ch.startElement(MULTIPAGE_URI, "page", MULTIPAGE_PREFIX + ":page", att);
					outputImage(image, imageToMetadataMap.get(image), input);
					ch.endElement(MULTIPAGE_URI, "page", MULTIPAGE_PREFIX + ":page");
				}
				ch.endElement(PNG_URI, "multipage", MULTIPAGE_PREFIX + ":multipage");
			} else {
				outputImage(src, imageToMetadataMap.get(src), input);
			}
		} else {
			throw new IOException("Input file is not a valid TIFF - JAI cannot find tiff_directory property");
		}
	}

	void outputImage(RenderedOp src, TIFFDirectory tiffDir, InputSource tiffSource) throws SAXException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RenderedOp imageOp;
		try {
			// Encode the file as a PNG image.
			imageOp = JAI.create("encode", src, baos, "PNG", null);
		} catch (Exception x) {
			// For some reason JAI can throw RuntimeExceptions on bad data.
			throw new SAXException(x);
		}

		AttributesImpl att = new AttributesImpl();
		att.addAttribute(PNG_URI, BasicImageNormaliser.DESCRIPTION_TAG_NAME, BasicImageNormaliser.DESCRIPTION_TAG_NAME, "CDATA",
		                 BasicImageNormaliser.PNG_DESCRIPTION_CONTENT);
		ContentHandler ch = getContentHandler();
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		ch.startElement(PNG_URI, PNG_TAG, PNG_PREFIX + ":" + PNG_TAG, att);

		InputStreamEncoder.base64Encode(is, ch);
		outputTiffMetadata(ch, tiffDir, tiffSource);

		ch.endElement(PNG_URI, PNG_TAG, PNG_PREFIX + ":" + PNG_TAG);
		imageOp.dispose();
		src.dispose();
		baos.close();
		is.close();
	}

	private void outputTiffMetadata(ContentHandler ch, TIFFDirectory tiffDir, InputSource tiffSource) throws SAXException, IOException {
		TIFFField[] fieldArr = tiffDir.getFields();
		if (fieldArr.length > 0) {
			AttributesImpl atts = new AttributesImpl();
			ch.startElement(PNG_URI, METADATA_TAG, PNG_PREFIX + ":" + METADATA_TAG, atts);
			for (int tagIndex = 0; tagIndex < fieldArr.length; tagIndex++) {
				int tagID = fieldArr[tagIndex].getTag();

				// We are primarily interested in the XMP and EXIF tags
				if (tagID == TiffTagUtilities.XMP_TAG_ID) {
					byte[] byteArr = fieldArr[tagIndex].getAsBytes();
					String xmpStr = new String(byteArr).trim();

					outputXMPMetadata(ch, xmpStr);
				} else if (tagID == TiffTagUtilities.EXIF_IFD_TAG_ID) {
					long exifIFDOffset = fieldArr[tagIndex].getAsLong(0);
					outputEXIFMetadata(ch, exifIFDOffset, tiffSource);
				}
			}

			ch.endElement(PNG_URI, METADATA_TAG, PNG_PREFIX + ":" + METADATA_TAG);
		}
	}

	private void outputEXIFMetadata(ContentHandler ch, long exifIFDOffset, InputSource tiffSource) throws IOException, SAXException {
		// JAI does not provide a mechanism for retrieving the IFD referenced in the EXIF IFD tag
		// (the EXIF IFD tag is a pointer to an IFD structure separate from the main IFD structure),
		// and the EXIF tags are not retrieved when using the getFields method. Thus we need to manually
		// retrieve the EXIF data.
		// Unfortunately the TIFF specification is such that the EXIF tags could be located in almost
		// any part of the TIFF file, and may not appear in sequential order. An InputStream is of not much use
		// if we are to be jumping back and forth through the file, so instead we will write out the TIFF to a
		// temporary file and us a RandomAccessFile to extract the EXIF tags.

		AttributesImpl atts = new AttributesImpl();
		ch.startElement(EXIF_URI, EXIF_ROOT_TAG, EXIF_PREFIX + ":" + EXIF_ROOT_TAG, atts);

		try {
			InputStream tiffStream = tiffSource.getByteStream();
			File tempTiffFile = File.createTempFile("tiff_source", ".tiff");
			tempTiffFile.deleteOnExit();
			FileOutputStream tempTiffOutput = new FileOutputStream(tempTiffFile);

			byte[] buffer = new byte[10 * 1024];
			int bytesRead = tiffStream.read(buffer);
			while (bytesRead > 0) {
				tempTiffOutput.write(buffer, 0, bytesRead);
				bytesRead = tiffStream.read(buffer);
			}
			tempTiffOutput.flush();
			tempTiffOutput.close();

			RandomAccessFile tiffRAF = new RandomAccessFile(tempTiffFile, "r");
			tiffRAF.seek(0);
			byte[] identifierBytes = new byte[2];
			bytesRead = tiffRAF.read(identifierBytes);
			if (bytesRead != 2) {
				throw new IOException("Temporary TIFF file could not be read: " + tempTiffFile.getAbsolutePath());
			}

			boolean useBigEndianOrdering = false;
			if (identifierBytes[0] == 0x4D && identifierBytes[1] == 0x4D) {
				useBigEndianOrdering = true;
			} else if (identifierBytes[0] == 0x49 && identifierBytes[1] == 0x49) {
				useBigEndianOrdering = false;
			} else {
				throw new IOException("Temporary TIFF file has an invalid header.");
			}

			processExifIFD(ch, tiffRAF, exifIFDOffset, useBigEndianOrdering);
		} catch (IOException iex) {
			String errorMessage = "EXIF Metadata could not be added due to an exception: " + iex.getMessage();
			char[] errorMessageChars = errorMessage.toCharArray();
			ch.characters(errorMessageChars, 0, errorMessageChars.length);
		} catch (SAXException sex) {
			String errorMessage = "EXIF Metadata could not be added due to an exception: " + sex.getMessage();
			char[] errorMessageChars = errorMessage.toCharArray();
			ch.characters(errorMessageChars, 0, errorMessageChars.length);
		} finally {
			ch.endElement(EXIF_URI, EXIF_ROOT_TAG, EXIF_PREFIX + ":" + EXIF_ROOT_TAG);
		}
	}

	private void processExifIFD(ContentHandler ch, RandomAccessFile tiffRAF, long ifdOffset, boolean useBigEndianOrdering) throws IOException,
	        SAXException {
		long currentOffset = ifdOffset;
		tiffRAF.seek(ifdOffset);
		byte[] intBuffer = new byte[2];
		int bytesRead = tiffRAF.read(intBuffer);

		if (bytesRead != 2) {
			throw new IOException("Could not read EXIF IFD Tag Entry Count at offset" + currentOffset);
		}

		currentOffset += 2;
		int tagEntryCount = getIntFromBytes(intBuffer, useBigEndianOrdering);

		for (int i = 0; i < tagEntryCount; i++) {
			byte[] tagBytes = new byte[TIFF_TAG_SIZE];
			bytesRead = tiffRAF.read(tagBytes);
			if (bytesRead != TIFF_TAG_SIZE) {
				throw new IOException("Could not read tag entry at offset" + currentOffset);
			}
			currentOffset += TIFF_TAG_SIZE;
			int tagID = getIntFromBytes(tagBytes, TAG_ENTRY_TAG_ID_OFFSET, useBigEndianOrdering);
			int dataType = getIntFromBytes(tagBytes, TAG_ENTRY_DATA_TYPE_OFFSET, useBigEndianOrdering);
			long dataCount = getLongFromBytes(tagBytes, TAG_ENTRY_DATA_COUNT_OFFSET, useBigEndianOrdering);

			byte[] tagValueBytes = new byte[4];
			System.arraycopy(tagBytes, TAG_ENTRY_DATA_OFFSET, tagValueBytes, 0, 4);

			String tagValue = "";
			try {
				tagValue = getEXIFStringValue(tagID, tagValueBytes, tiffRAF, dataType, dataCount, useBigEndianOrdering);
			} catch (Exception ex) {
				tagValue = "INVALID TAG DATA";
			}
			outputEXIFTag(ch, tagID, dataType, tagValue);

			// Ensure that we are at the right spot in the RAF
			tiffRAF.seek(currentOffset);
		}
	}

	private void outputEXIFTag(ContentHandler ch, int tagID, int dataType, String tagValue) throws IOException, SAXException {
		String tagName = TiffTagUtilities.getTagName(tagID);
		if (tagName == null) {
			tagName = "Unknown Tag";
		}
		AttributesImpl atts = new AttributesImpl();
		ch.startElement(EXIF_URI, tagName, EXIF_PREFIX + ":" + tagName, atts);
		char[] tagValueChars = tagValue.toCharArray();
		ch.characters(tagValueChars, 0, tagValueChars.length);
		ch.endElement(EXIF_URI, tagName, EXIF_PREFIX + ":" + tagName);
	}

	private void outputXMPMetadata(ContentHandler ch, String xmpStr) throws SAXException {
		XMLReader reader = null;
		try {
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		} catch (ParserConfigurationException x) {
			throw new SAXException(x);
		}

		// If we can't write out an element we want to fail immediately
		AttributesImpl atts = new AttributesImpl();
		ch.startElement(PNG_URI, XMP_TAG, PNG_PREFIX + ":" + XMP_TAG, atts);

		try {
			// Do not load external DTDs
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// If we don't do this we get multiple startDocuments occuring
			XMLFilterImpl filter = new XMLFilterImpl() {
				@Override
                public void startDocument() {
				}

				@Override
                public void endDocument() {
				}
			};
			filter.setContentHandler(ch);
			filter.setParent(reader);
			reader.setContentHandler(filter);
			StringReader xmpReader = new StringReader(xmpStr);
			InputSource xmpSource = new InputSource(xmpReader);
			reader.parse(xmpSource);
		} catch (IOException iex) {
			String errorMessage = "XMP Metadata could not be added as the XML could not be parsed. Error message: " + iex.getMessage();
			char[] errorMessageChars = errorMessage.toCharArray();
			ch.characters(errorMessageChars, 0, errorMessageChars.length);
		} catch (SAXException sex) {
			String errorMessage = "XMP Metadata could not be added as the XML could not be parsed. Error message: " + sex.getMessage();
			char[] errorMessageChars = errorMessage.toCharArray();
			ch.characters(errorMessageChars, 0, errorMessageChars.length);
		} finally {
			ch.endElement(PNG_URI, XMP_TAG, PNG_PREFIX + ":" + XMP_TAG);
		}
	}

	private int getIntFromBytes(byte[] intBytes, boolean useBigEndianOrdering) {
		assert intBytes.length == 2;

		// Java's bytes are signed. We will be reading unsigned bytes, so we need to convert.
		int[] intArr = new int[2];
		for (int i = 0; i < intArr.length; i++) {
			intArr[i] = intBytes[i] & 0xff;
		}

		int retInt = 0;
		if (useBigEndianOrdering) {
			retInt = (intArr[0] << 8) + intArr[1];
		} else {
			retInt = (intArr[1] << 8) + intArr[0];
		}

		return retInt;
	}

	private int getIntFromBytes(byte[] byteArr, int offset, boolean useBigEndianOrdering) {
		byte[] intBytes = new byte[2];
		System.arraycopy(byteArr, offset, intBytes, 0, 2);
		return getIntFromBytes(intBytes, useBigEndianOrdering);
	}

	private long getLongFromBytes(byte[] longBytes, boolean useBigEndianOrdering) {
		assert longBytes.length == 4;

		// Java's bytes are signed. We will be reading unsigned bytes, so we need to convert.
		int[] intArr = new int[4];
		for (int i = 0; i < intArr.length; i++) {
			intArr[i] = longBytes[i] & 0xff;
		}

		long retLong = 0;
		if (useBigEndianOrdering) {
			retLong = (intArr[0] << 24) + (intArr[1] << 16) + (intArr[2] << 8) + intArr[3];
		} else {
			retLong = (intArr[3] << 24) + (intArr[2] << 16) + (intArr[1] << 8) + intArr[0];
		}

		return retLong;
	}

	private long getLongFromBytes(byte[] byteArr, int offset, boolean useBigEndianOrdering) {
		byte[] longBytes = new byte[4];
		System.arraycopy(byteArr, offset, longBytes, 0, 4);
		return getLongFromBytes(longBytes, useBigEndianOrdering);
	}

	private String getEXIFStringValue(int tagID, byte[] byteArr, RandomAccessFile tiffRAF, int dataType, long dataCount, boolean useBigEndianOrdering)
	        throws IOException {
		String retValue = "";

		// Special cases for certain EXIF tags - eg value lookup, a table
		// structure etc

		try {
			switch (tagID) {
			case TiffTagUtilities.OECF_TAG_ID: // OECF - values in a table structure
				retValue = getTabularExifStringValue(byteArr, tiffRAF, dataCount, useBigEndianOrdering);
				break;
			case TiffTagUtilities.COMPONENTS_CONFIGURATION_TAG_ID: // ComponentsConfiguration - lookup table
				// 4 byte values which are indices for a table lookup
				String componentsConfigurationValue = "";
				for (int i = 0; i < byteArr.length; i++) {
					componentsConfigurationValue += TiffTagUtilities.COMPONENTS_CONFIG_LOOKUP[i];
				}
				retValue = componentsConfigurationValue;
				break;
			case TiffTagUtilities.METERING_MODE_TAG_ID: // MeteringMode - lookup table
				int meteringModeIndex = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				if (meteringModeIndex == 255) {
					retValue = "Other";
				} else {
					retValue = TiffTagUtilities.METERING_MODE_LOOKUP[meteringModeIndex];
				}
				break;
			case TiffTagUtilities.LIGHT_SOURCE_TAG_ID: // LightSource - lookup table
				int lightSourceIndex = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				if (lightSourceIndex == 255) {
					retValue = "Other light source";
				} else {
					retValue = TiffTagUtilities.LIGHT_SOURCE_LOOKUP[lightSourceIndex];
				}
				break;
			case TiffTagUtilities.FLASH_TAG_ID: // Flash - lookup table
				retValue = getFlashEXIFStringValue(byteArr, useBigEndianOrdering);
				break;
			case TiffTagUtilities.SUBJECT_AREA_TAG_ID: // SubjectArea - mode-based values
				retValue = getSubjectAreaEXIFStringValue(byteArr, tiffRAF, dataCount, useBigEndianOrdering);
				break;
			case TiffTagUtilities.COLOR_SPACE_TAG_ID: // ColorSpace - 1 for sRGB, 65535 for Uncalibrated
				int colourSpaceValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				if (colourSpaceValue == 1) {
					retValue = "sRGB";
				} else if (colourSpaceValue == 65535) {
					retValue = "Uncalibrated";
				}
				break;
			case TiffTagUtilities.SPATIAL_FREQUENCY_TAG_ID: // SpatialFrequencyResponse - table structure
				retValue = getTabularExifStringValue(byteArr, tiffRAF, dataCount, useBigEndianOrdering);
				break;
			case TiffTagUtilities.FOCAL_PLANE_RES_UNIT_TAG_ID: // FocalPlaneResolutionUnit - lookup table
				int focalPlaneResUnitValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.FOCAL_PLANE_RES_UNIT_LOOKUP[focalPlaneResUnitValue];
				break;
			case TiffTagUtilities.SENSING_METHOD_TAG_ID: // SensingMethod - lookup table
				int sensingMethodValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.SENSING_METHOD_LOOKUP[sensingMethodValue];
				break;

			case TiffTagUtilities.CFA_PATTERN_TAG_ID: // CFAPattern - lookup table
				retValue = getCFAPatternEXIFStringValue(byteArr, tiffRAF, dataCount, useBigEndianOrdering);
				break;

			case TiffTagUtilities.CUSTOM_RENDERED_TAG_ID: // CustomRendered - lookup table
				int customRenderedValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.CUSTOM_RENDERED_LOOKUP[customRenderedValue];
				break;
			case TiffTagUtilities.EXPOSURE_MODE_TAG_ID: // ExposureMode - lookup table
				int exposureModeValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.EXPOSURE_MODE_LOOKUP[exposureModeValue];
				break;
			case TiffTagUtilities.WHITE_BALANCE_TAG_ID: // WhiteBalance - lookup table
				int whiteBalanceValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.WHITE_BALANCE_LOOKUP[whiteBalanceValue];
				break;
			case TiffTagUtilities.SCENE_CAPTURE_TYPE_TAG_ID: // SceneCaptureType - lookup table
				int sceneCaptureTypeValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.SCENE_CAPTURE_TYPE_LOOKUP[sceneCaptureTypeValue];
				break;
			case TiffTagUtilities.GAIN_CONTROL_TAG_ID: // GainControl - lookup table
				int gainControlValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.GAIN_CONTROL_LOOKUP[gainControlValue];
				break;
			case TiffTagUtilities.CONTRAST_TAG_ID: // Contrast - lookup table
				int contrastValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.CONTRAST_LOOKUP[contrastValue];
				break;
			case TiffTagUtilities.SATURATION_TAG_ID: // Saturation - lookup table
				int saturationValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.SATURATION_LOOKUP[saturationValue];
				break;
			case TiffTagUtilities.SHARPNESS_TAG_ID: // Sharpness - lookup table
				int sharpnessValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.SHARPNESS_LOOKUP[sharpnessValue];
				break;
			case TiffTagUtilities.SUBJECT_DISTANCE_RANGE_TAG_ID: // SubjectDistanceRange - lookup table
				int subjectDistanceRangeValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
				retValue = TiffTagUtilities.SUBJECT_DISTANCE_RANGE_LOOKUP[subjectDistanceRangeValue];
				break;
			default:
				retValue = getDefaultEXIFStringValue(byteArr, tiffRAF, dataType, dataCount, useBigEndianOrdering);
			}
		} catch (ArrayIndexOutOfBoundsException aiex) {

		}

		return retValue;
	}

	private String getCFAPatternEXIFStringValue(byte[] byteArr, RandomAccessFile tiffRAF, long dataCount, boolean useBigEndianOrdering)
	        throws IOException {
		// The CFA Pattern value consists of:
		// Two short, being the grid width and height of the repeated pattern.
		// Next, for every pixel in that pattern, an short identification code, representing a colour.
		StringBuilder subjectAreaBuilder = new StringBuilder();

		long cfaValueOffset = getLongFromBytes(byteArr, useBigEndianOrdering);

		tiffRAF.seek(cfaValueOffset);
		byte[] cfaDimensionsArr = new byte[4];
		tiffRAF.read(cfaDimensionsArr);

		int cfaWidth = getIntFromBytes(cfaDimensionsArr, 0, useBigEndianOrdering);
		int cfaHeight = getIntFromBytes(cfaDimensionsArr, 2, useBigEndianOrdering);

		tiffRAF.seek(cfaValueOffset + 4);
		byte[] cfaData = new byte[cfaWidth * cfaHeight];
		tiffRAF.read(cfaData);

		for (int row = 0; row < cfaHeight; row++) {
			StringBuilder rowBuilder = new StringBuilder();
			for (int column = 0; column < cfaWidth; column++) {
				String colourName = TiffTagUtilities.CFA_PATTERN_LOOKUP[cfaData[row * column + column]];
				rowBuilder.append(colourName);
				rowBuilder.append(" ");
			}
			subjectAreaBuilder.append(rowBuilder.toString().trim());
			subjectAreaBuilder.append("\n");
		}

		return subjectAreaBuilder.toString();

	}

	private String getSubjectAreaEXIFStringValue(byte[] byteArr, RandomAccessFile tiffRAF, long dataCount, boolean useBigEndianOrdering)
	        throws IOException {
		String subjectAreaValue = "";
		switch ((int) dataCount) {
		case 2:
			// Location of main subject is given as coordinates
			int subjectXCoord = getIntFromBytes(byteArr, 0, useBigEndianOrdering);
			int subjectYCoord = getIntFromBytes(byteArr, 2, useBigEndianOrdering);
			subjectAreaValue = "Subject location: (" + subjectXCoord + ", " + subjectYCoord + ")";
			break;
		case 3:
			// Main subject is represented by a circle, with the first two values giving the coordinates of the centre,
			// and the third value giving the diameter of the circle.
			long circleValueOffset = getLongFromBytes(byteArr, useBigEndianOrdering);
			tiffRAF.seek(circleValueOffset);
			byte[] circleDataArr = new byte[(int) dataCount * 2];
			tiffRAF.read(circleDataArr);
			int circleXCoord = getIntFromBytes(circleDataArr, 0, useBigEndianOrdering);
			int circleYCoord = getIntFromBytes(circleDataArr, 2, useBigEndianOrdering);
			int circleDiameter = getIntFromBytes(circleDataArr, 4, useBigEndianOrdering);
			subjectAreaValue = "Subject circle centre: (" + circleXCoord + ", " + circleYCoord + "), diameter: " + circleDiameter;
			break;
		case 4:
			// Main subject is represented by a rectangle, with the first two values giving the coordinates of the
			// centre,
			// the third value giving the width and the fourth value giving the height of the rectangle.
			long rectangleValueOffset = getLongFromBytes(byteArr, useBigEndianOrdering);
			tiffRAF.seek(rectangleValueOffset);
			byte[] rectangleDataArr = new byte[(int) dataCount * 2];
			tiffRAF.read(rectangleDataArr);
			int rectangleXCoord = getIntFromBytes(rectangleDataArr, 0, useBigEndianOrdering);
			int rectangleYCoord = getIntFromBytes(rectangleDataArr, 2, useBigEndianOrdering);
			int rectangleWidth = getIntFromBytes(rectangleDataArr, 4, useBigEndianOrdering);
			int rectangleHeight = getIntFromBytes(rectangleDataArr, 6, useBigEndianOrdering);
			subjectAreaValue =
			    "Subject rectangle centre: (" + rectangleXCoord + ", " + rectangleYCoord + "), area (WxH): " + rectangleWidth + "x" + rectangleHeight;
			break;
		}

		return subjectAreaValue;
	}

	private String getTabularExifStringValue(byte[] byteArr, RandomAccessFile tiffRAF, long dataCount, boolean useBigEndianOrdering)
	        throws IOException {
		// The data in byteArr is an offset into the tiffRAF which points to the tabular data.
		// The tabular data is in the following structure:
		// - Two SHORTs, indicating respectively number of columns, and number of rows.
		// - For each column, the column name in a null-terminated ASCII string.
		// - For each cell, an SRATIONAL value.

		StringBuilder tabularStringBuilder = new StringBuilder();

		long tabularValueOffset = getLongFromBytes(byteArr, useBigEndianOrdering);
		tiffRAF.seek(tabularValueOffset);
		byte[] dataArr = new byte[(int) dataCount];
		tiffRAF.read(dataArr);

		int columnCount = getIntFromBytes(dataArr, 0, useBigEndianOrdering);
		int rowCount = getIntFromBytes(dataArr, 2, useBigEndianOrdering);
		int byteIndex = 4;

		// Column headers
		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
			StringBuilder columnHeaderBuilder = new StringBuilder();
			while (dataArr[byteIndex] != 0) {
				columnHeaderBuilder.append((char) dataArr[byteIndex]);
				byteIndex++;
			}
			tabularStringBuilder.append(columnHeaderBuilder);
			if (columnIndex < columnCount - 1) {
				tabularStringBuilder.append("\t");
			}
			byteIndex++;
		}
		tabularStringBuilder.append("\n");

		// Cell values (one SRATIONAL in each)
		for (int rowIndex = 0; rowIndex < rowCount; rowCount++) {
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				long firstRationalComponent = getLongFromBytes(dataArr, byteIndex, useBigEndianOrdering);
				byteIndex += 4;
				long secondRationalComponent = getLongFromBytes(dataArr, byteIndex, useBigEndianOrdering);
				byteIndex += 4;
				Rational cellRational = new Rational(firstRationalComponent, secondRationalComponent);
				tabularStringBuilder.append(cellRational.doubleValue());
				if (columnIndex < columnCount - 1) {
					tabularStringBuilder.append("\t");
				}
			}
			tabularStringBuilder.append("\n");
		}

		return tabularStringBuilder.toString();
	}

	private String getFlashEXIFStringValue(byte[] byteArr, boolean useBigEndianOrdering) {
		StringBuilder flashStringBuilder = new StringBuilder();
		int flashValue = getIntFromBytes(byteArr, 0, useBigEndianOrdering);

		// bit 0
		int flashFiredVal = flashValue & 0x01;

		// bits 1 and 2
		int returnedLightVal = (flashValue & 0x06) >> 1;

		// bits 3 and 4
		int flashModeVal = (flashValue & 0x18) >> 3;

		// bit 5
		int flashFunctionVal = (flashValue & 0x20) >> 5;

		// bit 6
		int redEyeModeVal = (flashValue & 0x40) >> 6;

		flashStringBuilder.append(TiffTagUtilities.FLASH_FIRED_LOOKUP[flashFiredVal]);
		flashStringBuilder.append(", " + TiffTagUtilities.FLASH_RETURNED_LIGHT_LOOKUP[returnedLightVal]);
		flashStringBuilder.append(", " + TiffTagUtilities.FLASH_MODE_LOOKUP[flashModeVal]);
		flashStringBuilder.append(", " + TiffTagUtilities.FLASH_FUNCTION_LOOKUP[flashFunctionVal]);
		flashStringBuilder.append(", " + TiffTagUtilities.FLASH_RED_EYE_LOOKUP[redEyeModeVal]);

		return flashStringBuilder.toString();
	}

	private String getDefaultEXIFStringValue(byte[] byteArr, RandomAccessFile tiffRAF, int dataType, long dataCount, boolean useBigEndianOrdering)
	        throws IOException {
		StringBuilder retValueBuilder = new StringBuilder();
		byte[] valueBytes;

		// If the data takes up more than 4 bytes, then the array of bytes passed in contains an offset to the actual
		// data
		long byteCount = getByteCount(dataType, dataCount);
		if (byteCount > 4) {
			long dataOffset = getLongFromBytes(byteArr, useBigEndianOrdering);
			tiffRAF.seek(dataOffset);
			valueBytes = new byte[(int) byteCount];
			int bytesRead = tiffRAF.read(valueBytes);

			if (bytesRead != byteCount) {
				throw new IOException("Data Type " + dataType + " has a data count of " + dataCount + " but only " + bytesRead + " bytes were read");
			}
		} else {
			valueBytes = byteArr;
		}

		switch (dataType) {
		case TIFFField.TIFF_UNDEFINED:
			retValueBuilder.append(new String(valueBytes));
			break;
		case TIFFField.TIFF_ASCII:
			// ASCII values are null-terminated
			retValueBuilder.append(new String(valueBytes, 0, valueBytes.length - 1));
			break;
		case TIFFField.TIFF_FLOAT: // Not used by EXIF... just return byte representation
		case TIFFField.TIFF_DOUBLE: // Not used by EXIF... just return byte representation
		case TIFFField.TIFF_BYTE:
		case TIFFField.TIFF_SBYTE:
			for (int byteIndex = 0; byteIndex < dataCount; byteIndex++) {
				retValueBuilder.append(valueBytes[byteIndex]);
				if (byteIndex < dataCount - 1) {
					retValueBuilder.append(", ");
				}
			}
			break;
		case TIFFField.TIFF_SHORT:
		case TIFFField.TIFF_SSHORT:
			for (int shortIndex = 0; shortIndex < dataCount; shortIndex++) {
				int tiffShort = getIntFromBytes(valueBytes, shortIndex * 2, useBigEndianOrdering);
				retValueBuilder.append(tiffShort);
				if (shortIndex < dataCount - 1) {
					retValueBuilder.append(", ");
				}
			}
			break;
		case TIFFField.TIFF_LONG:
		case TIFFField.TIFF_SLONG:
			for (int longIndex = 0; longIndex < dataCount; longIndex++) {
				long tiffLong = getLongFromBytes(valueBytes, longIndex * 4, useBigEndianOrdering);
				retValueBuilder.append(tiffLong);
				if (longIndex < dataCount - 1) {
					retValueBuilder.append(", ");
				}
			}
			break;
		case TIFFField.TIFF_RATIONAL:
		case TIFFField.TIFF_SRATIONAL:
			for (int rationalIndex = 0; rationalIndex < dataCount; rationalIndex++) {
				long tiffRationalFirstComponent = getLongFromBytes(valueBytes, rationalIndex * 8, useBigEndianOrdering);
				long tiffRationalSecondComponent = getLongFromBytes(valueBytes, rationalIndex * 8 + 4, useBigEndianOrdering);
				retValueBuilder.append(new Rational(tiffRationalFirstComponent, tiffRationalSecondComponent));
				if (rationalIndex < dataCount - 1) {
					retValueBuilder.append(", ");
				}
			}
			break;
		default:
			retValueBuilder.append(new String(valueBytes));
		}
		return retValueBuilder.toString();
	}

	private long getByteCount(int dataType, long dataCount) {
		int dataSize = 0;
		switch (dataType) {
		case TIFFField.TIFF_BYTE:
		case TIFFField.TIFF_ASCII:
		case TIFFField.TIFF_SBYTE:
		case TIFFField.TIFF_UNDEFINED:
			dataSize = 1;
			break;
		case TIFFField.TIFF_SHORT:
		case TIFFField.TIFF_SSHORT:
			dataSize = 2;
			break;
		case TIFFField.TIFF_LONG:
		case TIFFField.TIFF_SLONG:
		case TIFFField.TIFF_FLOAT:
			dataSize = 4;
			break;
		case TIFFField.TIFF_RATIONAL:
		case TIFFField.TIFF_SRATIONAL:
		case TIFFField.TIFF_DOUBLE:
			dataSize = 8;
			break;
		}
		return dataSize * dataCount;
	}
}
