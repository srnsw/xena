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

package au.gov.naa.digipres.xena.kernel.type;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.ToXml;
import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Represents one type of input that Xena can deal with.
 *
 * @see TypeManager
 * @created    March 29, 2002
 */
public abstract class Type implements XmlSerializable, Comparable {
	/**
	 *  Get the name of this type.
	 *
	 * @return    The name value
	 */
	public abstract String getName();

	/**
	 * Get the MIME type of this Type.
	 * 
	 * @return the MIME Type
	 */
	public abstract String getMimeType();

	/**
	 * Returns the FileType name as a String
	 *
	 * @return FileType name
	 */
	@Override
    public String toString() {
		return getName();
	}

	/**
	 * Returns an Element representing the XML for this Type
	 * 
	 * return Type XML
	 */
	public Element toXml() {
		return ToXml.toXmlBasic(this);
	}

	/**
	 * Creates a Type from the given XML (currently not implemented)
	 */
	public void fromXml(Element element) {
		// Nothing
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass()) && getName().equals(((Type) obj).getName());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		int retVal;
		if (o instanceof Type) {
			Type compType = (Type) o;
			retVal = this.getName().compareTo(compType.getName());
		} else {
			retVal = -1;
		}
		return retVal;
	}

}
