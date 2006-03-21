package au.gov.naa.digipres.xena.plugin.naa;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.jdom.Namespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.FoundException;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;

/**
 * Wraps the XML according to NAA policy. Firstly, an inner package with NAA
 * meta data, then outside that a checksum.
 *
 * @author Chris Bitmead
 */
public class NaaPackageWrapNormaliser extends AbstractMetaDataWrapper {

    
    
	final Namespace nameSpace = Namespace.getNamespace(NaaTagNames.PACKAGE_PREFIX, NaaTagNames.PACKAGE_URI);
	NaaInnerWrapNormaliser innerWrapNormaliser = new NaaInnerWrapNormaliser(this);
	ChecksumContentHandler checksumContentHandler = new ChecksumContentHandler();
	NaaOuterWrapNormaliser outerWrapNormaliser = new NaaOuterWrapNormaliser();

	
    
	public String toString() {
		return "NAA Package Wrapper";
	}

	public void setContentHandler(ContentHandler handler) {
		super.setContentHandler(innerWrapNormaliser);
		int level = 0;
		try {
			// JRW - really annoying in debug so making it slightly better
			Integer levelObj = (Integer)getProperty("http://xena/level");
			if (levelObj != null)
			{
				level = levelObj.intValue();
			}
        } catch (SAXNotSupportedException x) {
            //sysout - print stack trace in case of exception getting 'level' property.
			x.printStackTrace();
		} catch (SAXNotRecognizedException x) {
			//sysout - print stack trace in case of exception getting 'level' property.
            x.printStackTrace();
		}
		if (level == 0) {
			innerWrapNormaliser.setParent(this);
			innerWrapNormaliser.setContentHandler(checksumContentHandler);
			checksumContentHandler.setParent(innerWrapNormaliser);
			checksumContentHandler.setContentHandler(outerWrapNormaliser);
			outerWrapNormaliser.setParent(checksumContentHandler);
			outerWrapNormaliser.setContentHandler(handler);
			outerWrapNormaliser.setMD5(checksumContentHandler.getMD5());
		} else {
			innerWrapNormaliser.setParent(this);
			innerWrapNormaliser.setContentHandler(handler);
		}
	}

	public ContentHandler getContentHandler() {
		return outerWrapNormaliser.getContentHandler();
	}

    public String getOpeningTag(){
        return NaaTagNames.PACKAGE_PACKAGE;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return getTagContents(input, NaaTagNames.DCIDENTIFIER);
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return getTagContents(input, NaaTagNames.DCSOURCE);
    }
    
    
    public String getTagContents(XenaInputSource input, String tag) throws XenaException {
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
