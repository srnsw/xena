package au.gov.naa.digipres.xena.plugin.image.tiff;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TiffTagUtilities
{
	public static final int XMP_TAG_ID = 700;
	public static final int EXIF_IFD_TAG_ID = 34665;
	public static final int GPS_IFD_TAG_ID = 34853;

	// Special-case EXIF tags
	public static final int OECF_TAG_ID = 34856;
	public static final int COMPONENTS_CONFIGURATION_TAG_ID = 37121;
	public static final int METERING_MODE_TAG_ID = 37383;
	public static final int LIGHT_SOURCE_TAG_ID = 37384;
	public static final int FLASH_TAG_ID = 37385;
	public static final int SUBJECT_AREA_TAG_ID = 37396;
	public static final int COLOR_SPACE_TAG_ID = 40961;
	public static final int SPATIAL_FREQUENCY_TAG_ID = 41484;
	public static final int FOCAL_PLANE_RES_UNIT_TAG_ID = 41488;
	public static final int SENSING_METHOD_TAG_ID = 41495;
	public static final int CFA_PATTERN_TAG_ID = 41730;
	public static final int CUSTOM_RENDERED_TAG_ID = 41985;
	public static final int EXPOSURE_MODE_TAG_ID = 41986;
	public static final int WHITE_BALANCE_TAG_ID = 41987;
	public static final int SCENE_CAPTURE_TYPE_TAG_ID = 41990;
	public static final int GAIN_CONTROL_TAG_ID = 41991;
	public static final int CONTRAST_TAG_ID = 41992;
	public static final int SATURATION_TAG_ID = 41993;
	public static final int SHARPNESS_TAG_ID = 41994;
	public static final int SUBJECT_DISTANCE_RANGE_TAG_ID = 41996;
	
	public static final String[] COMPONENTS_CONFIG_LOOKUP = {"", "Y", "Cb", "Cr", "R", "G", "B"};
	public static final String[] METERING_MODE_LOOKUP = {"Unknown", "Average", "CenterWeightedAverage", "Spot", "MultiSpot", "Pattern", "Partial"};
	public static final String[] LIGHT_SOURCE_LOOKUP = {"Unknown", "Daylight", "Fluorescent", "Tungsten (incandescent light)", "Flash",
														"", "", "", "", "Fine Weather", "Cloudy weather", "Shade", 
														"Daylight fluorescent (D 5770 - 7100K)", "Day white fluorescent (N 4600 - 5400K)", 
														"Cool white fluorescent (W 3900 - 4500K)", "White fluorescent (WW 3200 - 3700K)", 
														"", "Standard light A", "Standard light B", "Standard light C", "D55", "D65", "D75",
														"D50", "ISO studio tungsten"};
	public static final String[] FLASH_FIRED_LOOKUP = {"Flash did not fire", "Flash fired"};
	public static final String[] FLASH_RETURNED_LIGHT_LOOKUP = {"No strobe return detection function", "reserved", 
																"Strobe return light not detected", "Strobe return light detected"};
	public static final String[] FLASH_MODE_LOOKUP = {"Unknown", "Compulsory flash firing", "Compulsory flash suppression", "Auto mode"};
	public static final String[] FLASH_FUNCTION_LOOKUP = {"Flash function present", "No flash function"};
	public static final String[] FLASH_RED_EYE_LOOKUP = {"No red-eye reduction mode or unknown", "Red-eye reduction mode"};
	public static final String[] FOCAL_PLANE_RES_UNIT_LOOKUP = {"", "No absolute unit of measurement", "Inch", "Centimeter"};
	public static final String[] SENSING_METHOD_LOOKUP = {"", "Not defined", "One-chip color area sensor", "Two-chip color area sensor",
														  "Three-chip color area sensor", "Color sequential area sensor", 
														  "Trilinear sensor", "Color sequential linear sensor"};
	public static final String[] CFA_PATTERN_LOOKUP = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "White"};
	public static final String[] CUSTOM_RENDERED_LOOKUP = {"Normal process", "Custom process"};
	public static final String[] EXPOSURE_MODE_LOOKUP = {"Auto exposure", "Manual exposure", "Auto bracket"};
	public static final String[] WHITE_BALANCE_LOOKUP = {"Auto white balance", "Manual white balance"};
	public static final String[] SCENE_CAPTURE_TYPE_LOOKUP = {"Standard", "Landscape", "Portrait", "Night scene"};
	public static final String[] GAIN_CONTROL_LOOKUP = {"None", "Low gain up", "High gain up", "Low gain down", "High gain down"};
	public static final String[] CONTRAST_LOOKUP = {"Normal", "Soft", "Hard"};
	public static final String[] SATURATION_LOOKUP = {"Normal", "Low saturation", "High saturation"};
	public static final String[] SHARPNESS_LOOKUP = {"Normal", "Soft", "Hard"};
	public static final String[] SUBJECT_DISTANCE_RANGE_LOOKUP = {"Unknown", "Macro", "Close view", "Distant view"};

	private static final String TAG_ID_PROP_SUFFIX = ".ID";
	private static Map<Long, String> tagNameMap;
	
	
	private static Map<Long, String> getTagNameMap() throws IOException
	{
		if (tagNameMap == null)
		{
			Properties props = new Properties();
			InputStream inStream = TiffTagUtilities.class.getResourceAsStream("tags.properties");
			props.load(inStream);
			
			tagNameMap = new HashMap<Long, String>();
			Enumeration propEnum = props.propertyNames();
			while (propEnum.hasMoreElements())
			{
				String propName = propEnum.nextElement().toString();
				if (propName.endsWith(TAG_ID_PROP_SUFFIX))
				{
					long tagID = Long.parseLong(props.getProperty(propName));
					String tagName = propName.substring(0, propName.indexOf(TAG_ID_PROP_SUFFIX));
					tagNameMap.put(tagID, tagName);
				}
			}
		}
		return tagNameMap;
	}
	
	public static String getTagName(long tagID) throws IOException
	{
		Map<Long, String> tagNameMap = getTagNameMap();
		return tagNameMap.get(tagID);
	}
		
}
