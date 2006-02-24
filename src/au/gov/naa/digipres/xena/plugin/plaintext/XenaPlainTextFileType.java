package au.gov.naa.digipres.xena.plugin.plaintext;
import java.util.Iterator;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena plaintext instances.
 * @author Chris Bitmead
 */
public class XenaPlainTextFileType extends XenaFileType {
	public XenaPlainTextFileType() {
		addSortType(
			new SortType() {
			public String getName() {
				return "Number of Lines";
			}

			public int comparison(Element e1, Element e2) {
				java.util.List list1 = e1.getChildren("line");
				java.util.List list2 = e2.getChildren("line");
				return list1.size() - list2.size();
			}
		}
		);
		addSortType(
			new SortType() {
			public String getName() {
				return "ASCII";
			}

			/**
			 *  Compare the number of lines in each Element.
			 */
			public int comparison(Element e1, Element e2) {
				java.util.List list1 = e1.getChildren("line");
				java.util.List list2 = e2.getChildren("line");
				Iterator it1 = list1.iterator();
				Iterator it2 = list2.iterator();
				int rtn = 0;
				while (it1.hasNext() && it2.hasNext()) {
					if (!it1.hasNext()) {
						rtn = 1;
						break;
					} else if (!it2.hasNext()) {
						rtn = -1;
						break;
					}
					Element l1 = (Element)it1.next();
					Element l2 = (Element)it2.next();
					String s1 = l1.getText();
					String s2 = l2.getText();
					rtn = s1.compareTo(s2);
					if (rtn != 0) {
						break;
					}
				}
				return rtn;
			}
		}
		);
	}

	public String getTag() {
		return "plaintext:plaintext";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/plaintext/1.0";
	}
}
