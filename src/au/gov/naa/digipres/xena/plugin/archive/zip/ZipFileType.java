package au.gov.naa.digipres.xena.plugin.archive.zip;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a Zip-like archive - includes .zip and .jar files
 *
 * @author Justin Waddell
 */
public class ZipFileType extends FileType 
{
	public String getName() 
	{
		return "Zip";
	}
    
    public String getMimeType() 
    {
        return "application/zip";
    }
    
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "zip";
	}
    
}
