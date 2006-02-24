package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing a Xena string XML object.
 *
 * @author Chris Bitmead
 */
public class XenaStringFileType extends XenaFileType {

	public XenaStringFileType() {
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/string/1.0";
	}

	public String getTag() {
		return "string:string";
	}
}
