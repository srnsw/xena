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

package au.gov.naa.digipres.xena.kernel.type;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Represents one of the Xena file types.
 *
 * @created    June 5, 2002
 */
abstract public class XenaFileType extends FileType implements XmlSerializable {
	protected List<SortType> sortTypes = new ArrayList<SortType>();

	@Override
    public String getName() {
		return "Xena type, tag -->> " + getTag();
	}

	/**
	 *  Get a list of all the sort types available.
	 *
	 * @return    The sortTypes value
	 */
	public List<SortType> getSortTypes() {
		return sortTypes;
	}

	@Override
    public String getMimeType() {
		return "application/xena";
	}

	@Override
    public Element toXml() {
		Element rtn = new Element("object");
		rtn.setAttribute("object", this.getClass().getName());
		return rtn;
	}

	@Override
    public void fromXml(Element el) {
		// Class cls = Class.forName(el.getAttributeValue("type"));
		// XXXXXXXXXX
	}

	abstract public String getTag();

	/**
	 *  Add a new available sort type.
	 *
	 * @param  type  The feature to be added to the SortType attribute
	 */
	public void addSortType(SortType type) {
		sortTypes.add(type);
	}

	abstract public String getNamespaceUri();

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension() {
		return "xena";
	}

	/**
	 *  Subclasses of XenaFileType should create instances of SortType in order to
	 *  specify the types of available sorting for that FileType.
	 *
	 * @created    April 2, 2002
	 */
	static public abstract class SortType implements Comparator {
		/**
		 *  A name describing the type of sort comparison algorithm.
		 *
		 * @return    The name value
		 */
		public abstract String getName();

		/**
		 *  Compare two XML Elements. Return -1, 0 or 1 based on the rules in
		 *  java.util.Comparator.
		 *
		 * @param  e1  Description of Parameter
		 * @param  e2  Description of Parameter
		 * @return     Description of the Returned Value
		 */
		public abstract int comparison(Element e1, Element e2);

		public int compare(Object o1, Object o2) {
			return comparison((Element) o1, (Element) o2);
		}

		@Override
        public String toString() {
			return getName();
		}
	}
}
