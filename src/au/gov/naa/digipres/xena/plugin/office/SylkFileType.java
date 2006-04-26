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
		return "SYLK";
	}
}
