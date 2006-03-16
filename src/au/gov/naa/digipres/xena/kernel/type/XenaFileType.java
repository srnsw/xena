package au.gov.naa.digipres.xena.kernel.type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Represents one of the Xena file types. Name is always Xena-something.
 *
 * @author     Chris
 * @created    June 5, 2002
 */
abstract public class XenaFileType extends FileType implements XmlSerializable {
	protected List sortTypes = new ArrayList();

	public String getName() {
		return "Xena type, tag -->> " + getTag();
	}

	/**
	 *  Get a list of all the sort types available.
	 *
	 * @return    The sortTypes value
	 */
	public List getSortTypes() {
		return sortTypes;
	}

	public String getMimeType() {
		return "application/xena";
	}

	public Element toXml() {
		Element rtn = new Element("object");
		rtn.setAttribute("object", this.getClass().getName());
		return rtn;
	}

	public void fromXml(Element el) {
//		Class cls = Class.forName(el.getAttributeValue("type"));
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

	/**
	 *  Subclasses of XenaFileType should create instances of SortType in order to
	 *  specify the types of available sorting for that FileType.
	 *
	 * @author     Chris
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
			return comparison((Element)o1, (Element)o2);
		}

		public String toString() {
			return getName();
		}
	}
}
