package au.gov.naa.digipres.xena.plugin.archive.tar;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a Zip-like archive - includes .zip and .jar files
 *
 * @author Justin Waddell
 */
public class TarFileType extends FileType 
{
	public String getName() 
	{
		return "Tar";
	}
    
    public String getMimeType() 
    {
        return "application/tar";
    }
    
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "tar";
	}
    
}
