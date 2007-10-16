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

package au.gov.naa.digipres.xena.plugin.xml;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent random XML files that have been put through Xena.
 * Actually, there's no distinction between a random XML file and a Xena
 * XML file. Note to self: Try and remember why we need this class.
 *
 */
public class XenaXmlFileType extends XenaFileType {
	@Override
    public String getNamespaceUri() {
		return null;
	}

	@Override
    public String getTag() {
		return "Unknown";
	}
}
