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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html;

import au.gov.naa.digipres.xena.kernel.XenaException;
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
	 * This default method just returns the extension associated with the type associated with this
	 * denormaliser, but concrete classes may need to determine the extension from the file being
	 * denormalised, as some denormalisers can produce multiple file types (eg an office normalised
	 * file could be a word processor file, a spreadsheet or a presentation).
	 * 
	 * @return output file extension
	 */
	@Override
    public String getOutputFileExtension(XenaInputSource xis) throws XenaException {
		return normaliserManager.getOutputType(this.getClass()).fileExtension();
	}

}
