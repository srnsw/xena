package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent the Microsoft PST file format.
 *
 * @author Chris Bitmead
 */
public class PstFileType extends FileType {
	public String getName() {
		return "Pst Outlook";
	}
}
