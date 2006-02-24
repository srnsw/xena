package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent JPEG files.
 *
 * @author Chris Bitmead
 */
public class JpegFileType extends FileType {
	public String getName() {
		return "JPEG";
	}

	public String getMimeType() {
		return "image/jpeg";
	}

	public String fileExtension() {
		return "jpg";
	}
}
