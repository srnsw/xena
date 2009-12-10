/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * Class to convert Xena XHTML to HTML. Just need to extend XmlDeNormaliser, which will handle a simple
 * output of the original XML stored in the Xena file.
 *
 */
public class XenaHtmlToHtmlDeNormaliser extends XmlDeNormaliser {

	@Override
	public String getName() {
		return "Xena HTML Denormaliser";
	}

	/**
	 * Return the file extension which should be used for the file exported by this denormaliser.
	 * 
	 * This class extends the XmlDeNormaliser class which returns an extension of "xml". 
	 * We will thus just override with the default implementation.
	 * 
	 * @return output file extension
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) {
		return normaliserManager.getOutputType(this.getClass()).fileExtension();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeIdentifier()
	 */
	@Override
	public String getDoctypePublicIdentifier() {
		return XHTML_DOCTYPE_PUBLIC_ID;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeName()
	 */
	@Override
	public String getDoctypeName() {
		return XHTML_DOCTYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.util.XmlDeNormaliser#getDoctypeSystemIdentifier()
	 */
	@Override
	public String getDoctypeSystemIdentifier() {
		return XHTML_DOCTYPE_SYSTEM_ID;
	}

}
