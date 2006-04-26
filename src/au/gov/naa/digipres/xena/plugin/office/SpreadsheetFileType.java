package au.gov.naa.digipres.xena.plugin.office;

/**
 * Type to represent a spreadsheet.
 *
 * @author Chris Bitmead
 */
public class SpreadsheetFileType extends OfficeFileType {

	public SpreadsheetFileType() {
	}

	public String getName() {
		return "Spreadsheet";
	}
    

    public String getMimeType() {
        return "application/vnd.ms-excel";
    }

	@Override
	public String getOfficeConverterName()
	{
		return "calc8";
	}
}
