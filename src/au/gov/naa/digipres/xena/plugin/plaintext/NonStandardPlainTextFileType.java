package au.gov.naa.digipres.xena.plugin.plaintext;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent plaintext files.
 *
 * @author Justin Waddell
 */
public class NonStandardPlainTextFileType extends FileType {
	public NonStandardPlainTextFileType() {
	}

	public String getName() {
		return "Non-Standard PlainText";
	}

	public String getMimeType() {
		return "text/plain";
	}

	public String fileExtension() {
		return "txt";
	}
}
