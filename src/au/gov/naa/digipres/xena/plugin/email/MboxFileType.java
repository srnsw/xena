package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Class to represent a MBOX email folder file.
 *
 * @author Chris Bitmead
 */
public class MboxFileType extends FileType {
	public String getName() {
		return "Mbox Email";
	}


    public String getMimeType() {
        return "application/mbox";
    }
}
