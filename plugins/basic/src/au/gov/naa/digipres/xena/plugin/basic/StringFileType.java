package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type representing a string.
 *
 * @author Chris Bitmead
 */
public class StringFileType extends FileType {
	public StringFileType() {
	}

	public String getName() {
		return "String";
	}

	public String getMimeType() {
		return "text/plain";
	}
}
