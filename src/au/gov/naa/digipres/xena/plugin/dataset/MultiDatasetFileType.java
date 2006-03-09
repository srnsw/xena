package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type representing a collection of Xena dataset files.
 *
 * @author Chris Bitmead
 */
public class MultiDatasetFileType extends FileType {
	public String getName() {
		return "Multi Dataset";
	}
    


    public String getMimeType() {
        return "text/plain";
    }
}
