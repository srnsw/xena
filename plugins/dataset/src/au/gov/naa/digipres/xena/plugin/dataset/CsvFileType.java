package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Class representing a Comma Separated File. In reality, the file may be
 * delimited by any character, but Comma and Tab are the most common cases
 * catered for.
 *
 * @author Chris Bitmead
 */
public class CsvFileType extends FileType {
	public String getName() {
		return "Old Csv";
	}

	public String getMimeType() {
		return "text/plain";
	}
}
