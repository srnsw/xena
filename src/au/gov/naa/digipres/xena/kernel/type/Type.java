package au.gov.naa.digipres.xena.kernel.type;
import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.ToXml;
import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Represents one type of input that Xena can deal with.
 *
 * @see TypeManager
 * @author     Chris Bitmead
 * @created    March 29, 2002
 */
public abstract class Type implements XmlSerializable {
	/**
	 *  Get the user name of this sort type.
	 *
	 * @return    The name value
	 */
	public abstract String getName();

	/**
	 *  Returns the FileType name as a String
	 *
	 * @return    FileType name
	 */
	public String toString() {
		return getName();
	}

	public Element toXml() {
		return ToXml.toXmlBasic(this);
	}

	public void fromXml(Element element) {
		// Nothing
	}

	public int hashCode() {
		return getClass().hashCode();
	}

	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass()) && getName().equals(((Type)obj).getName());
	}
    
    public abstract String getMimeType();
    
}
