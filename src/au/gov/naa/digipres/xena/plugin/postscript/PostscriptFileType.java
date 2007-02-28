package au.gov.naa.digipres.xena.plugin.postscript;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent postScript files.
 * 
 * postScript File Type extends from FileType
 * 
 * @see au.gov.naa.digipres.xena.kernel.type.FileType
 * 
 * @authors Kamaj Jayakantha de Mel and Quang Phuc Tran(Eric)
 * 
 * @since 14-Feb-2007
 * @version 1.0
 * 
 */
public class PostscriptFileType extends FileType {

	/**
	 * @return String : File Type name
	 */
	@Override
	public String getName() {
		return "Postscript";
	}

	/**
	 * @return String : File Extention
	 */
	@Override
	public String fileExtension() {
		return "ps";
	}

}
