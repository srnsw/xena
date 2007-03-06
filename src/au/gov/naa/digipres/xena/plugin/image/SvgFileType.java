package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent an SVG file.
 *
 * @author Justin Waddell
 */
public class SvgFileType extends FileType {
	public String getName() {
		return "SVG";
	}


    public String getMimeType() {
        return "image/svg";
    }


	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "svg";
	}
    
    
}
