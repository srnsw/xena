package au.gov.naa.digipres.xena.plugin.naa;
// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 * @author Chris Bitmead
 * @author Justin Waddell
 */
public class NaaSignedAipUnwrapFilter extends XMLFilterImpl 
{
	private int contentLevel = 0;

    public String toString()
    {
        return "NAA Package - Unwrapper. Looking for wrapper:signed-aip";
    }
    
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
	{
		if (contentLevel > 0)
		{
			super.startElement(namespaceURI, localName, qName, atts);
		}
		
		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) 
		{
			contentLevel++;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException 
	{
		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) 
		{
			contentLevel--;
			
		}		

		if (contentLevel > 0)
		{
			super.endElement(namespaceURI, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException 
	{
		if (contentLevel > 0) 
		{
			super.characters(ch, start, length);
		}
	}

}
