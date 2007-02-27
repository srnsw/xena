package au.gov.naa.digipres.xena.kernel.type;

/**
 *  Subclasses represents one type of file that Xena can deal with.
 *
 * @author     Chris Bitmead
 * @created    March 29, 2002
 */
public abstract class FileType extends Type {

	@Override
	public String getMimeType() {
		return "unknown/unknown";
	}
    
	/**
	 * Return the extension generally used for files of this type
	 * @return file extension
	 */
	public String fileExtension() {
        return "";   
    }
}
