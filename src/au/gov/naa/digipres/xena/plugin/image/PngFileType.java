package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent PNG files.
 *
 * @author Chris Bitmead
 */
public class PngFileType extends FileType {
	public String getName() {
		return "PNG";
	}

	public String getMimeType() {
		return "image/png";
	}

	public String fileExtension() {
		return "png";
	}
}
