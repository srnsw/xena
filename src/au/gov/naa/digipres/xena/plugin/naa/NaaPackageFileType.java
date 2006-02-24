package au.gov.naa.digipres.xena.plugin.naa;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent a Xena package.
 *
 * @author Chris bitmead
 */
public class NaaPackageFileType extends XenaFileType {

	public NaaPackageFileType() {
	}

	public String getTag() {
		return "package:package";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/package/1.0";
	}
}
