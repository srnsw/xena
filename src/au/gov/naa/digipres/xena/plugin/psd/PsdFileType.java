package au.gov.naa.digipres.xena.plugin.psd;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent PSD files.
 *  
 * PSD File Type extends from FileType
 * @see au.gov.naa.digipres.xena.kernel.type.FileType
 * 
 * @author Kamaj Jayakantha de Mel
 * @since 09-Feb-2007
 * 
 * @version 1.0
 * 
 */
public class PsdFileType extends FileType {

	/**
	 * @return String : File Type name
	 */
	@Override
	public String getName() {
		return "PSD";
	}

	/**
	 * @return String File Extension
	 */
	@Override
	public String fileExtension() {
		return "psd";
	}

}
