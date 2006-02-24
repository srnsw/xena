package au.gov.naa.digipres.xena.plugin.plaintext;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent plaintext files.
 *
 * @author Chris Bitmead
 */
public class PlainTextFileType extends FileType {
	public PlainTextFileType() {
	}

	public String getName() {
		return "PlainText";
	}

	public String getMimeType() {
		return "text/plain";
	}

	public String fileExtension() {
		return "txt";
	}
}
