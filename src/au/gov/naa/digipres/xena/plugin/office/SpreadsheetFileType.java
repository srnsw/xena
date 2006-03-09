package au.gov.naa.digipres.xena.plugin.office;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a spreadsheet.
 *
 * @author Chris Bitmead
 */
public class SpreadsheetFileType extends FileType {

	public SpreadsheetFileType() {
	}

	public String getName() {
		return "Spreadsheet";
	}
    

    public String getMimeType() {
        return "application/vnd.ms-excel";
    }
}
