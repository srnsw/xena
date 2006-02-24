package au.gov.naa.digipres.xena.plugin.html;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena website summary file.
 *
 * @author Chris Bitmead
 */
public class XenaWebSiteFileType extends XenaFileType {
	public XenaWebSiteFileType() {
	}

	public String getTag() {
		return "website:website";
	}

	public String getNamespaceUri() {
		return null;
	}
}
