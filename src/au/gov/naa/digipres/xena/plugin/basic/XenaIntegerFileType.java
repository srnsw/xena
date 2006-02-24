package au.gov.naa.digipres.xena.plugin.basic;
import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing a Xena integer XML object.
 *
 * @author Chris Bitmead
 */
public class XenaIntegerFileType extends XenaFileType {
	public XenaIntegerFileType() {
		addSortType(
			new SortType() {
			public String getName() {
				return "Numeric";
			}

			public int comparison(Element e1, Element e2) {
				try {
					return Integer.parseInt(e1.getText()) - Integer.parseInt(e2.getText());
				} catch (NumberFormatException x) {
					x.printStackTrace();
					return 0;
				}
			}
		}
		);
	}

	public String getTag() {
		return "integer:integer";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/integer/1.0";
	}
}
