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

package au.gov.naa.digipres.xena.kernel;

import org.jdom.Element;

/**
 *  Interface for objects that want to customize serialization to XML.
 *
 * @created    22 September 2002
 */
public interface XmlSerializable {
	/**
	 * Convert object to an XML tree.
	 * @return JDOM tree
	 */
	public Element toXml();

	/**
	 * Populate this object from an XML tree.
	 * @param element JDOM tree
	 */
	public void fromXml(Element element);
}
