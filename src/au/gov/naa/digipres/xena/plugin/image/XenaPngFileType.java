package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena png file type.
 *
 * @author Chris Bitmead
 */
public class XenaPngFileType extends XenaFileType {
	public String getTag() {
		return "png:png";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/png/1.0";
	}
}
