package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing a Xena database type.
 *
 * @author Chris Bitmead
 */
public class XenaDatabaseFileType extends XenaFileType {
	public String getTag() {
		return ("database:database");
	}

	public String getNamespaceUri() {
		return null;
	}
}
