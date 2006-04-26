/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.FoundException;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public class TagContentFinder {
    
    /**
     * Find the contents of a tag using 
     * @param input
     * @param tag
     * @return
     * @throws XenaException
     */
    //TODO: aak - TagContentFinder - do this better in a more efficient way. That would be good.
    static    public String getTagContents(XenaInputSource input, String tag) throws XenaException {
        final String myTag = tag;
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setContentHandler(new XMLFilterImpl() {
                String result = "";
                boolean found = false;
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws
                SAXException {
                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    
                    if (qName.equals(myTag)) {
                        found = true;
                    }
                }
                public void characters(char ch[], int start, int length) throws SAXException {
                    if (found) {
                        result += new String(ch, start, length);
                    }
                }
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (found) {
                        throw new FoundException(result);
                    }
                }
            });
            try {
                reader.parse(input);
            } catch (FoundException x) {
                input.close();
                return x.getName();
            }
            input.close();
        } catch (SAXException x) {
            throw new XenaException(x);
        } catch (ParserConfigurationException x) {
            throw new XenaException(x);
        } catch (IOException x) {
            throw new XenaException(x);
        }
        return null;
    }
    
    
}
