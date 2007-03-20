package au.gov.naa.digipres.xena.plugin.office;

/**
 * Type to represent an SYLK spreadsheet.
 *
 * @author Chris Bitmead
 */
public class SylkFileType extends OfficeFileType {
	public String getName() {
		return "SYLK Spreadsheet";
	}
    
    public String getMimeType() {
        return "text/plain";
    }

	@Override
	public String getOfficeConverterName()
	{
		return "calc8";
	}
	
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension()
	{
		return "ods";
	}

}
