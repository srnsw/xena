package au.gov.naa.digipres.xena.plugin.psd;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * create a Xena file type for xena to convert to.
 *  
 * Xena PSD File Type extends from XenaFileType
 * @see au.gov.naa.digipres.xena.kernel.type
 * 
 * @author Kamaj Jayakantha de Mel
 * @since 09-Feb-2007
 * 
 * @version 1.0
 * 
 */
public class XenaPsdFileType extends XenaFileType {

	/**
	 * @return String: Name Space URI
	 */
	@Override
	public String getNamespaceUri() {
		return "psd:psd";
	}

	@Override
	public String getTag() {
		return "http://preservation.naa.gov.au/psd/1.0";
	}

}
