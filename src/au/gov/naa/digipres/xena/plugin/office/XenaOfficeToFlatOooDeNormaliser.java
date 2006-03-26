package au.gov.naa.digipres.xena.plugin.office;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
/**
 * Convert a Xena OOo file to native file. This is a no-op because Xena OOo is
 * a native OOo file. Although... I have seen a bug in OOo sometimes that OOo
 * doesn't like empty tags: <font/>. That would be the place to look if any bugs
 * crop up.
 *
 * @author Chris Bitmead
 */
public class XenaOfficeToFlatOooDeNormaliser extends AbstractDeNormaliser {

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Xena office to Flat OOo denormaliser";
    }
}
