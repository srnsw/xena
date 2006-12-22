package au.gov.naa.digipres.xena.plugin.image;
import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.TempFileWriter;

/**
 * Normaliser to convert XML to Xena XML. Basically a no-op because random XML
 * can be considered Xena XML.
 *
 * @author Chris Bitmead
 */
public class SvgNormaliser extends AbstractNormaliser {
	public String getName() {
		return "SVG";
	}

	public void parse(InputSource input, NormaliserResults results)
			throws java.io.IOException, org.xml.sax.SAXException
	{
		try
		{
			// Check SVG validity by creating a SVGDocument from the SVG file. An exception will be thrown if it is not valid.
			File tempFile = TempFileWriter.createTempFile(input);
			String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
	        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(parserClassName);
	        documentFactory.createDocument(tempFile.toURI().toString());
			
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

			// Do not load external DTDs
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// If we don't do this we get multiple startDocuments occuring
			XMLFilterImpl filter = new XMLFilterImpl() {
				public void startDocument()
				{
				}

				public void endDocument()
				{
				}
			};
			filter.setContentHandler(getContentHandler());
			filter.setParent(reader);
			reader.setContentHandler(filter);
			reader.parse(input);
		}
		catch (ParserConfigurationException x)
		{
			throw new SAXException(x);
		}
	}

}
