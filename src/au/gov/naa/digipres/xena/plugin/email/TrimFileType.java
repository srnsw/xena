package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a TRIM .mbx email file.
 *
 * @author Chris Bitmead
 */
public class TrimFileType extends FileType {

	public TrimFileType() {
	}

	public String getName() {
		return "TRIM Email";
	}
    
    public String getMimeType() {
        return "text/plain";
    }
}
