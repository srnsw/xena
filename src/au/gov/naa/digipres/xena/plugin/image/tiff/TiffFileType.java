package au.gov.naa.digipres.xena.plugin.image.tiff;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent Java supported image types other than the core JPEG and PNG
 *
 * @author Chris Bitmead
 */
public class TiffFileType extends FileType {
	public TiffFileType() {
	}

	public String getName() {
		return "TIFF";
	}

    public String getMimeType() {
        return "image/tiff";
    }
}
