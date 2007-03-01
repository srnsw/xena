/*
 * Created on 28/02/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.pdf;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class PdfDeNormaliser extends BinaryDeNormaliser
{
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		String elementName = PdfToXenaPdfNormaliser.PDF_PREFIX + ":" + PdfToXenaPdfNormaliser.PDF_PREFIX;
		if (elementName.equals(qName))
		{
			start();
		}
	}

	public String toString()
	{
		return "PDF";
	}

}
