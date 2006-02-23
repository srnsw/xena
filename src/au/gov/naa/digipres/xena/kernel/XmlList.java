package au.gov.naa.digipres.xena.kernel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 *  An XML Serializable ArrayList.
 *
 * @author     Chris Bitmead
 * @created    9 September 2002
 */

public class XmlList extends ArrayList implements XmlSerializable {
	public XmlList() {
	}

	public XmlList(Collection c) {
		super(c);
	}

	public void fromXml(Element element) {
		List lst = element.getChildren("element");
		Iterator it = lst.iterator();
		clear();
		while (it.hasNext()) {
			Element el = (Element)it.next();
			Element object = el.getChild("object");
			add(ToXml.fromXml(object));
		}
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
