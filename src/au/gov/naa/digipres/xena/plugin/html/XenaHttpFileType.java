package au.gov.naa.digipres.xena.plugin.html;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena HTTP web site summary.
 *
 * @author Chris Bitmead
 */
public class XenaHttpFileType extends XenaFileType {
	public XenaHttpFileType() {
	}

	public String getTag() {
		return "http:http";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/http/1.0";
	}
}
