package au.gov.naa.digipres.xena.kernel.type;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent plaintext files.
 *
 * @author Chris Bitmead
 */
public class DefaultFileType extends FileType {
	public DefaultFileType() {
	}

	public String getName() {
		return "Default type";
	}

	public String getMimeType() {
		return "text/plain";
	}

	public String fileExtension() {
		return "*";
	}
}
