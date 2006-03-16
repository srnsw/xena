package au.gov.naa.digipres.xena.plugin.html;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

/**
 * Class to convert Xena XHTML to HTML. Actually a no-op, because XHTML is HTML.
 *
 * @author Chris Bitmead.
 */
public class XenaHtmlToHtmlDeNormaliser extends AbstractDeNormaliser {

    @Override
    public String getName() {
        return "Xena HTML Denormaliser";
    }
}
