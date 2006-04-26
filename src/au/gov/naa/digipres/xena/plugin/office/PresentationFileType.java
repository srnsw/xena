package au.gov.naa.digipres.xena.plugin.office;

/**
 * Type to represent a presentation file.
 *
 * @author Chris Bitmead
 */
public class PresentationFileType extends OfficeFileType {

	public PresentationFileType() {
	}

	public String getName() {
		return "Presentation";
	}

    public String getMimeType() {
        return "application/vnd.ms-powerpoint";
    }

	@Override
	public String getOfficeConverterName()
	{
		return "impress8";
	}
}
