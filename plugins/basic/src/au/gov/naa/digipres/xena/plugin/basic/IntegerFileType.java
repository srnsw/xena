package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type representing an integer.
 *
 * @author Chris Bitmead
 */
public class IntegerFileType extends FileType {

	public IntegerFileType() {
	}

	public String getName() {
		return "Integer";
	}

	public String getMimeType() {
		return "text/plain";
	}
}
