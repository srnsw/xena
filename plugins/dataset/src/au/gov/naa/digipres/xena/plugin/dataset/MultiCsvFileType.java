package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type representing a collection of CSV files.
 *
 * @author Chris Bitmead
 */
public class MultiCsvFileType extends FileType {
	public String getName() {
		return "Multi CSV";
	}

    public String getMimeType() {
        return "text/csv";
    }
    
}
