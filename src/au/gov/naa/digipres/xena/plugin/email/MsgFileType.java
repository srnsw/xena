package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent the Microsoft MSG file format.
 *
 * @author Chris Bitmead
 */
public class MsgFileType extends FileType {
	public String getName() {
		return "Microsoft MSG";
	}


    public String getMimeType() {
        return "application/msoutlook";
    }
}
