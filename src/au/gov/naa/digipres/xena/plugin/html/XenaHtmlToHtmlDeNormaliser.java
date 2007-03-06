package au.gov.naa.digipres.xena.plugin.html;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * Class to convert Xena XHTML to HTML. Just need to extend XmlDeNormaliser, which will handle a simple
 * output of the original XML stored in the Xena file.
 *
 * @author Justin Waddell
 */
public class XenaHtmlToHtmlDeNormaliser extends XmlDeNormaliser {

    @Override
    public String getName() {
        return "Xena HTML Denormaliser";
    }

	/**
     * Return the file extension which should be used for the file exported by this denormaliser.
     * This default method just returns the extension associated with the type associated with this
     * denormaliser, but concrete classes may need to determine the extension from the file being
     * denormalised, as some denormalisers can produce multiple file types (eg an office normalised
     * file could be a word processor file, a spreadsheet or a presentation).
     * 
     * @return output file extension
     */
    public String getOutputFileExtension(XenaInputSource xis) throws XenaException
    {
    	return normaliserManager.getOutputType(this.getClass()).fileExtension();
    }
    
}
