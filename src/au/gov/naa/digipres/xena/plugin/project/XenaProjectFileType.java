package au.gov.naa.digipres.xena.plugin.project;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent a Xena project file.
 *
 * @author Chris Bitmead
 */
public class XenaProjectFileType extends XenaFileType {
	public XenaProjectFileType() {
	}

	public String getTag() {
		return "Project";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/project/1.0";
	}
}
