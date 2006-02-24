package au.gov.naa.digipres.xena.plugin.html;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing Xena XHTML file type.
 *
 * @author Chris Bitmead
 */
public class XenaHtmlFileType extends XenaFileType {
	public XenaHtmlFileType() {
	}

	public String getTag() {
		return "html";
	}

	public String getNamespaceUri() {
		return null;
	}
}
