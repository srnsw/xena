package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type class representing a date/time value.
 *
 * @author Chris Bitmead
 */
public class DateTimeFileType extends FileType {

	public DateTimeFileType() {
	}

	public String getName() {
		return "Date/Time";
	}

	public String getMimeType() {
		return "text/plain";
	}
}
