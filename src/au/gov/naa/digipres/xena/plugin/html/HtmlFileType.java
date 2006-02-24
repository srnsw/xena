package au.gov.naa.digipres.xena.plugin.html;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a HTML file.
 *
 * @author Chris Bitmead
 */
public class HtmlFileType extends FileType {
	public HtmlFileType() {
	}

	public String getName() {
		return "HTML";
	}

	public String getMimeType() {
		return "text/html";
	}
}
