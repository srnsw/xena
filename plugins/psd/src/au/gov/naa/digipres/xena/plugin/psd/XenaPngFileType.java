package au.gov.naa.digipres.xena.plugin.psd;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * create a Xena file type for xena to convert to.
 *  
 * Xena PSD File Type extends from XenaFileType
 * @see au.gov.naa.digipres.xena.kernel.type
 *
 * @author NAA Digital Preservation
 */
public class XenaPngFileType extends XenaFileType {
	@Override
	public String getTag() {
		return "png:png";
	}

	@Override
	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/png/1.0";
	}
}
