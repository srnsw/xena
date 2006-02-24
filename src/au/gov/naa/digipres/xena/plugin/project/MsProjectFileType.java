package au.gov.naa.digipres.xena.plugin.project;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent MS Project files.
 *
 * @author Chris Bitmead
 */
public class MsProjectFileType extends FileType {
	public MsProjectFileType() {
	}

	public String getName() {
		return "Microsoft Project";
	}

	public String getMimeType() {
		return "application/vnd.ms-project";
	}

	public String fileExtension() {
		return "txt";
	}
}
