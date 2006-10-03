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
public class NaaPackageUnwrapFilter extends XMLFilterImpl 
{
	private boolean contentFound = false;
	private boolean nextFound = false;

    public String toString()
    {
        return "NAA Package - Unwrapper. Looking for package:content";
    }
    
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
	{
		if (contentFound)
		{
			nextFound = true;
		}
		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) 
		{
			contentFound = true;
		}
		if (pass()) 
		{
			super.startElement(namespaceURI, localName, qName, atts);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException 
	{
		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) 
		{
			contentFound = false;
			nextFound = false;
		}
		if (pass()) 
		{
			super.endElement(namespaceURI, localName, qName);
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException 
	{
		if (pass()) 
		{
			super.characters(ch, start, length);
		}
	}

	private boolean pass() 
	{
		return nextFound;
	}
}
