package au.gov.naa.digipres.xena.plugin.office;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;
import au.gov.naa.digipres.xena.util.UrlEncoder;

/**
 * Convert a Xena OOo file to native open document file.
 *
 * @author Chris Bitmead
 */
public class XenaOfficeToFlatOooDeNormaliser extends BinaryDeNormaliser
{

	private static final String EXTENSION_XPATH_STRING = "//opendocument/@extension";
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		String elementName = OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX + ":" + OfficeToXenaOooNormaliser.OPEN_DOCUMENT_PREFIX;
		if (elementName.equals(qName))
		{
			start();
		}
	}

	public String toString()
	{
		return "Office";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#getOutputFileExtension(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getOutputFileExtension(XenaInputSource xis) throws XenaException
	{
		try
		{
			File inputFile = xis.getFile();

			DocumentBuilder builder = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			// If the file originally had a space in it, it will have been converted to %20 in the xena file name.
			// However the parsing process assumes that this needs to be decoded, and converts it back into a space,
			// and consequently cannot find the file. We need to URL-encode the filename before we begin.
	        String uri = "file:" + inputFile.getAbsolutePath();
	        if (File.separatorChar == '\\') {
	            uri = uri.replace('\\', '/');
	        }
	        String encodedURI = UrlEncoder.encode(uri);
			Document xmlDoc = builder.parse(encodedURI);

			// Extract the extension from the XML
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(EXTENSION_XPATH_STRING, xmlDoc);
		}
		catch (Exception e)
		{
			throw new XenaException("Problem retrieving file extension of normalised office file.", e);
		}
	}
	
	

}
