/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.FoundException;

public class DefaultWrapper extends XMLFilterImpl implements XenaWrapper {

    public static String OPENINGTAG = "xena";
    
    public DefaultWrapper() {
        super();
    }

    public void setContentHandler(ContentHandler handler) {
        super.setContentHandler(handler);
    }
        
    public String toString() {
        return "Xena Default XML Wrapper";
    }

    public String getOpeningTag() {
        return OPENINGTAG;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return getSourceData(input, "input_source_uri");
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return getSourceData(input, "input_source_uri");
    }
    
    public String getSourceData(XenaInputSource input, String tagName) throws XenaException {   
        final String myTagName = tagName;
        try {
            //notout
            //System.out.println("Making reader...");
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setContentHandler(new XMLFilterImpl() {
                String result = "";
                boolean found = false;
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws
                    SAXException {
                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    if (qName.equals(myTagName)) {
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
        throw new XenaException("Could not get contents of tag:" +  myTagName + " in default wrapper");
    }


    private final static String DEFAULTWRAPPER = "Default Package Wrapper";
    
    public void startDocument() throws SAXException {
        try {
            XMLReader normaliser = (XMLReader)getProperty("http://xena/normaliser");
            if (normaliser == null) {
                throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
            }
            
            XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
            super.startDocument();
            File outfile = ((File)getProperty("http://xena/file"));

            if (outfile == null)  {
                throw new XenaException("Output file was null!");
            }
            if (xis.getFile() == null) {
                throw new XenaException("XIS input file was null!");
            }
            

            ContentHandler th = getContentHandler();
            AttributesImpl att = new AttributesImpl();
            th.startElement(null, "xena","xena", att);

            
            th.startElement(null, "meta_data", "meta_data", att);
            
            // give the name of the meta data wrapper...
            th.startElement(null, "meta_data_wrapper_name","meta_data_wrapper_name", att);
            th.characters(DEFAULTWRAPPER.toCharArray(), 0, DEFAULTWRAPPER.length());
            th.endElement(null, "meta_data_wrapper_name", "meta_data_wrapper_name");
            
            // give the class name of the normaliser
            th.startElement(null, "normaliser_name", "normaliser_name", att);
            th.characters(normaliser.getClass().getName().toCharArray(), 0, normaliser.getClass().getName().length());
            th.endElement(null, "normaliser_name", "normaliser_name");

            // give the input source uri of the current xis
            th.startElement(null, "input_source_uri", "input_source_uri", att);
            th.characters(xis.getSystemId().toCharArray(), 0, xis.getSystemId().length());
            th.endElement(null, "input_source_uri", "input_source_uri");
            
            th.endElement(null, "meta_data", "meta_data");
            
            th.startElement(null, "content","content", att);
            
            
        } catch (XenaException x) {
            throw new SAXException(x);
        }
    }

    public void endDocument() throws org.xml.sax.SAXException {
        XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
        File outfile = ((File)getProperty("http://xena/file"));
        //int level = ((Integer)getProperty("http://xena/level"));
        if (xis.getFile() != null || outfile != null) {
            ContentHandler th = getContentHandler();
            th.endElement(null, "content","content");
            th.endElement(null, "xena","xena");
        }
        super.endDocument();
    }

}
