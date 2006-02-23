package au.gov.naa.digipres.xena.kernel.type;

/**
 *  Subclasses represents one type of file that Xena can deal with.
 *
 * @author     Chris Bitmead
 * @created    March 29, 2002
 */
public abstract class FileType extends Type {
	public String getMimeType() {
		return "unknown/unknown";
	}

	public String fileExtension() {
     return null;   
    }
}
