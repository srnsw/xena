package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a directory containing a bunch of MBOX files.
 *
 * @author Chris Bitmead
 */
public class MboxDirFileType extends FileType {
	public String getName() {
		return "MBOX Directory";
	}
}
