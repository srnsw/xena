package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena jpeg file type.
 *
 * @author Chris Bitmead
 */
public class XenaJpegFileType extends XenaFileType {
	public String getTag() {
		return "jpeg:jpeg";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/jpeg/1.0";
	}
}
