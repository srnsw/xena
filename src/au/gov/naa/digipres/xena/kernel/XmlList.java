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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *  An XML Serializable ArrayList.
 *
 * @created    9 September 2002
 */

public class XmlList extends ArrayList implements XmlSerializable {

	public XmlList() {
	}

	public XmlList(Collection c) {
		super(c);
	}

	public void fromXml(Element element) {
		// List lst = element.getChildren("element");
		// Iterator it = lst.iterator();
		// clear();
		// while (it.hasNext()) {
		// Element el = (Element)it.next();
		// Element object = el.getChild("object");
		// add(ToXml.fromXml(object));
		// }
	}

	public Element toXml() {
		Element rtn = new Element("object");
		rtn.setAttribute(new Attribute("type", getClass().getName()));
		Iterator it = iterator();
		while (it.hasNext()) {
			Element sub = ToXml.toXml(it.next());
			Element element = new Element("element");
			if (sub != null) {
				element.addContent(sub);
			}
			rtn.addContent(element);
		}
		return rtn;
	}

}
