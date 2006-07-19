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
public abstract class Type implements XmlSerializable, Comparable
{
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
	public String toString() {
		return getName();
	}	
	
	/**
	 * Returns an Element representing the XML for this Type
	 * 
	 * return Type XML
	 */ 
	public Element toXml() 
	{
		return ToXml.toXmlBasic(this);
	}
    
	/**
	 * Creates a Type from the given XML (currently not implemented)
	 */
	public void fromXml(Element element) 
	{
		// Nothing
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass()) && getName().equals(((Type)obj).getName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o)
	{
		int retVal;
		if (!(o instanceof Type))
		{
			retVal = -1;
		}
		else
		{
			Type compType = (Type)o;
			retVal = this.getName().compareTo(compType.getName());
		}
		return retVal;
	}
	
	
}
