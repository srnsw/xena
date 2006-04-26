package au.gov.naa.digipres.xena.plugin.office;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena office files.
 *
 * @author Chris Bitmead
 */
public class XenaOooFileType extends XenaFileType {

	public XenaOooFileType() {
	}

	public String getTag() {
		return OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX + ":" + OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX;
	}

	public String getNamespaceUri() {
		return null;
	}
}
