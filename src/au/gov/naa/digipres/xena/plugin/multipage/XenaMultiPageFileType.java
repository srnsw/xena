package au.gov.naa.digipres.xena.plugin.multipage;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent a Xena multipage instance.
 *
 * @author Chris Bitmead
 */
public class XenaMultiPageFileType extends XenaFileType {
	public String getTag() {
		return "multipage:multipage";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/multipage/1.0";
	}
}
