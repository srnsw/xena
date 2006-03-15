package au.gov.naa.digipres.xena.plugin.xml;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Normaliser to convert XML to Xena XML. Basically a no-op because random XML
 * can be considered Xena XML.
 *
 * @author Chris Bitmead
 */
public class XmlToXenaXmlNormaliser extends AbstractNormaliser {
	public String getName() {
		return "XML";
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

			// Do not load external DTDs
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", 
			                  false);
			
			// If we don't do this we get multiple startDocuments occuring
			XMLFilterImpl filter = new XMLFilterImpl() {
				public void startDocument() {
				}

				public void endDocument() {
				}
			};
			filter.setContentHandler(getContentHandler());
			filter.setParent(reader);
			reader.setContentHandler(filter);
			reader.parse(input);
		} catch (ParserConfigurationException x) {
			throw new SAXException(x);
		}
	}

}
