/*
 * Created on 09/05/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;

/**
 * This class is used to mark files as unsupported by the NAA, and just binary normalise them.
 * This will enable them to be more easily found at a later stage and renormalised when an appropriate normaliser exists.
 * @author justinw5
 * created 09/05/2007
 * naa
 * Short desc of class:
 */
public class UnsupportedTypeNormaliser extends BinaryToXenaBinaryNormaliser
{

	@Override
	public String getName()
	{
		return "Unsupported Type Normaliser";
	}


}
