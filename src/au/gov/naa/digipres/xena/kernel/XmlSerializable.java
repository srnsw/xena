package au.gov.naa.digipres.xena.kernel;
import org.jdom.Element;

/**
 *  Interface for objects that want to customize serialization to XML.
 *
 * @author     Chris Bitmead
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
