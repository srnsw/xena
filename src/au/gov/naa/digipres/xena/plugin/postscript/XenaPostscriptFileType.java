package au.gov.naa.digipres.xena.plugin.postscript;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * create a Xena file type for xena to convert to.
 *  
 * Xena Postscript File Type extends from XenaFileType
 * 
 * @see au.gov.naa.digipres.xena.kernel.type
 *
 * @author Kamaj Jayakantha de Mel 
 * 
 * @since 14-Feb-2007
 * @version 1.0
 * 
 */
public class XenaPostscriptFileType extends XenaFileType {
	
	/**
	 * Get XenaFileType
	 * @return String : XenaFileType
	 */
	@Override
	public String getTag() {
		return "postscript:postscript";
	}

	/**
	 * Get getNamespaceUri
	 * @return String : getNamespaceUri
	 */
	@Override
	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/postscript/1.0";
	}
}
