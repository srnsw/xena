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

package au.gov.naa.digipres.xena.plugin.plaintext;

import java.util.Iterator;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena plaintext instances.
 */
public class XenaPlainTextFileType extends XenaFileType {
	public XenaPlainTextFileType() {
		addSortType(new SortType() {
			@Override
			public String getName() {
				return "Number of Lines";
			}

			@SuppressWarnings("unchecked")
			@Override
			public int comparison(Element e1, Element e2) {
				java.util.List<Element> list1 = e1.getChildren("line");
				java.util.List<Element> list2 = e2.getChildren("line");
				return list1.size() - list2.size();
			}
		});
		addSortType(new SortType() {
			@Override
			public String getName() {
				return "ASCII";
			}

			/**
			 *  Compare the number of lines in each Element.
			 */
			@SuppressWarnings("unchecked")
			@Override
			public int comparison(Element e1, Element e2) {
				java.util.List<Element> list1 = e1.getChildren("line");
				java.util.List<Element> list2 = e2.getChildren("line");
				Iterator<Element> it1 = list1.iterator();
				Iterator<Element> it2 = list2.iterator();
				int rtn = 0;
				while (it1.hasNext() && it2.hasNext()) {
					if (!it1.hasNext()) {
						rtn = 1;
						break;
					} else if (!it2.hasNext()) {
						rtn = -1;
						break;
					}
					Element l1 = it1.next();
					Element l2 = it2.next();
					String s1 = l1.getText();
					String s2 = l2.getText();
					rtn = s1.compareTo(s2);
					if (rtn != 0) {
						break;
					}
				}
				return rtn;
			}
		});
	}

	@Override
	public String getTag() {
		return "plaintext:plaintext";
	}

	@Override
	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/plaintext/1.0";
	}
}
