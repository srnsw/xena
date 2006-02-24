package au.gov.naa.digipres.xena.plugin.xml;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent random XML files that have been put through Xena.
 * Actually, there's no distinction between a random XML file and a Xena
 * XML file. Note to self: Try and remember why we need this class.
 *
 * @author Chris Bitmead
 */
public class XenaXmlFileType extends XenaFileType {
	public String getNamespaceUri() {
		return null;
	}

	public String getTag() {
		return "Unknown";
	}
}
