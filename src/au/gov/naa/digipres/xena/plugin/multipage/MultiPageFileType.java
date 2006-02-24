package au.gov.naa.digipres.xena.plugin.multipage;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a number of files suitable for input into the
 * MultiPageNormaliser.
 *
 * @author Chris Bitmead
 */
public class MultiPageFileType extends FileType {
	public String getName() {
		return "MultiPage";
	}
}
