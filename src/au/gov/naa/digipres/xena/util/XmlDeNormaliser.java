/*
 * Created on 06/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class XmlDeNormaliser extends AbstractDeNormaliser
{

	private TransformerHandler outputXMLWriter;
	
	
	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getName()
	 */
	@Override
	public String getName()
	{
		return "XML DeNormaliser";
	}

	/**
	 * 
	 */
    public String getOutputFileExtension(XenaInputSource xis) throws XenaException
    {
    	return "xml";
    }

    /* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException
	{
        // create our transform handler
        outputXMLWriter = null;
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try 
        {
            outputXMLWriter = transformFactory.newTransformerHandler();
        } 
        catch (TransformerConfigurationException e) 
        {
            throw new SAXException("Unable to create transformerHandler due to transformer configuration exception.");
        }

        if (streamResult == null)
        {
        	throw new SAXException("StreamResult has not been initialised.");
        }
        else
        {
        	outputXMLWriter.setResult(streamResult);
        	outputXMLWriter.startDocument();
        }
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		outputXMLWriter.characters(ch, start, length);
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException
	{
		outputXMLWriter.endDocument();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		outputXMLWriter.endElement(namespaceURI, localName, qName);
	}


	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		outputXMLWriter.startElement(namespaceURI, localName, qName, atts);
	}
    
    
    

}
