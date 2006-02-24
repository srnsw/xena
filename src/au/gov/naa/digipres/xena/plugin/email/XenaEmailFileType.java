package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena email files.
 *
 * @author Chris Bitmead
 */
public class XenaEmailFileType extends XenaFileType {

	public XenaEmailFileType() {
	}

	public String getTag() {
		return "email:email";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/email/1.0";
	}
}
