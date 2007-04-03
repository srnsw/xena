package au.gov.naa.digipres.xena.plugin.archive.gzip;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a Zip-like archive - includes .zip and .jar files
 *
 * @author Justin Waddell
 */
public class GZipFileType extends FileType 
{
	public String getName() 
	{
		return "GZip";
	}
    
    public String getMimeType() 
    {
        return "application/gzip";
    }
    
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "gz";
	}
    
}
