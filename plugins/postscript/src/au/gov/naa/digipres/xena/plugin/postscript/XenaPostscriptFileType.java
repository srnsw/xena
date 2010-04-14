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
 * @author Kamaj Jayakantha de Mel
 * @author Matthew Oliver
 */

package au.gov.naa.digipres.xena.plugin.postscript;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * create a Xena file type for xena to convert to.
 *  
 * Xena Postscript File Type extends from XenaFileType
 * 
 * @see au.gov.naa.digipres.xena.kernel.type
 * 
 */
public class XenaPostscriptFileType extends XenaFileType {
	
	/**
	 * Get XenaFileType
	 * @return String : XenaFileType
	 */
	@Override
	public String getTag() {
		//return "postscript:postscript";
		return PostscriptNormaliser.POSTSCRIPT_PREFIX + ":" + PostscriptNormaliser.POSTSCRIPT_PREFIX;
	}

	/**
	 * Get getNamespaceUri
	 * @return String : getNamespaceUri
	 */
	@Override
	public String getNamespaceUri() {
		//return "http://preservation.naa.gov.au/postscript/1.0";
		return PostscriptNormaliser.POSTSCRIPT_URI;
	}
}
