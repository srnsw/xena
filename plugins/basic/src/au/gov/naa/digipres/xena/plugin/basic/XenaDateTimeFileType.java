package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing a Xena data-time XML object.
 *
 * @author Chris Bitmead
 */
public class XenaDateTimeFileType extends XenaFileType {

	public XenaDateTimeFileType() {
	}

	public String getTag() {
		return "date-time:date-time";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/date-time/1.0";
	}
}
