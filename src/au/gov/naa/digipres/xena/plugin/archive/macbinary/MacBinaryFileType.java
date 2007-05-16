package au.gov.naa.digipres.xena.plugin.archive.macbinary;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a Zip-like archive - includes .zip and .jar files
 *
 * @author Justin Waddell
 */
public class MacBinaryFileType extends FileType 
{
	public String getName() 
	{
		return "MacBinary";
	}
    
    public String getMimeType() 
    {
        return "application/macbinary";
    }
    
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "bin";
	}
    
}
