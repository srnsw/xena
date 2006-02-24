package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent Java supported image types other than the core JPEG and PNG
 *
 * @author Chris Bitmead
 */
public class LegacyImageFileType extends FileType {
	public LegacyImageFileType() {
	}

	public String getName() {
		return "Legacy Image";
	}
}
