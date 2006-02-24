package au.gov.naa.digipres.xena.plugin.office;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a presentation file.
 *
 * @author Chris Bitmead
 */
public class PresentationFileType extends FileType {

	public PresentationFileType() {
	}

	public String getName() {
		return "Presentation";
	}
}
