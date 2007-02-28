package au.gov.naa.digipres.xena.plugin.psd;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Convert Xena png into native PNG file
 *  
 * XenaPngDeNornaliser extends from AbstractDeNormaliser
 * @see au.gov.naa.digipres.xena.kernel.AbstractDeNormaliser
 * 
 * @author Kamaj Jayakantha de Mel
 * @since 12-Feb-2007
 * 
 * @version 1.0
 * 
 */
public class XenaPngDeNornaliser extends AbstractDeNormaliser {

	@Override
	public String getName() {
		return "PNG Denormaliser";
	}

}
