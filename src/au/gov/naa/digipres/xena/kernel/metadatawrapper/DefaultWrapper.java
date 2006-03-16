/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.FoundException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

public class DefaultWrapper extends AbstractMetaDataWrapper {

    public static final String OPENING_TAG = "xena";
    public static final String META_TAG = "meta_data";
    public static final String CONTENT_TAG = "content";
    public static final String META_DATA_WRAPPER_NAME_TAG = "meta_data_wrapper_name";
    public static final String NORMALISER_NAME_TAG = "normaliser_name";
    public static final String INPUT_SOURCE_URI_TAG = "input_source_uri";
    
    private final static String DEFAULTWRAPPER = "Default Package Wrapper";
    
    public String toString() {
        return "Xena Default XML Wrapper";
    }

    public String getOpeningTag() {
        return OPENING_TAG;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return getSourceData(input, INPUT_SOURCE_URI_TAG);
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return getSourceData(input, INPUT_SOURCE_URI_TAG);
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
            th.startElement(null, OPENING_TAG, OPENING_TAG, att);

            
            th.startElement(null, META_TAG, META_TAG, att);
            
            // give the name of the meta data wrapper...
            th.startElement(null, META_DATA_WRAPPER_NAME_TAG,META_DATA_WRAPPER_NAME_TAG, att);
            th.characters(DEFAULTWRAPPER.toCharArray(), 0, DEFAULTWRAPPER.length());
            th.endElement(null, META_DATA_WRAPPER_NAME_TAG, META_DATA_WRAPPER_NAME_TAG);
            
            // give the class name of the normaliser
            th.startElement(null, NORMALISER_NAME_TAG, NORMALISER_NAME_TAG, att);
            th.characters(normaliser.getClass().getName().toCharArray(), 0, normaliser.getClass().getName().length());
            th.endElement(null, NORMALISER_NAME_TAG, NORMALISER_NAME_TAG);

            // give the input source uri of the current xis
            th.startElement(null, INPUT_SOURCE_URI_TAG, INPUT_SOURCE_URI_TAG, att);

            String xisRelativeSystemId = "";
            try {
                java.net.URI uri = new java.net.URI(xis.getSystemId());
                if (uri.getScheme().equals("file")) {
                    File inputSourceFile = new File(uri);
                    String relativePath = null;
                    File baseDir;
                    /*
                     * Get the path location. 
                     * 
                     * First off, see if we can get a path from the filter manager, and get a relative path.
                     * If that doesnt work, try to get a legacy base path, and a relative path from that.
                     * If still no success, then we set the path to be the full path name.
                     * 
                     */
                    if (metaDataWrapperManager.getBasePathName() != null) {
                        try {
                            baseDir = new File(metaDataWrapperManager.getBasePathName());
                            if (baseDir != null) {
                                relativePath = FileName.relativeTo(baseDir, inputSourceFile);
                            }
                        } catch (IOException iox) {
                            //notout
                            //System.out.println("Could not get base path from the Filter manager.");
                            relativePath = null;
                        }
                    }
                    if (relativePath == null) {
                        try {
                            baseDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.SOURCE_DIR_STRING);
                            if (baseDir != null) {
                                relativePath = FileName.relativeTo(baseDir, inputSourceFile);
                            } 
                        } catch (IOException iox) {
                            //sysout
                            System.out.println("Could not get base path from Legacy Xena code.");
                            relativePath = null;
                        } catch (XenaException xe) {
                            //sysout
                            System.out.println("Could not get base path from Legacy Xena code.");
                            relativePath = null;
                        }
                    }
                    if (relativePath == null) {
                        relativePath = inputSourceFile.getAbsolutePath();
                    }
                    String encodedPath = null;
                    try {
                        encodedPath = au.gov.naa.digipres.xena.helper.UrlEncoder.encode(relativePath);
                    } catch (UnsupportedEncodingException x) {
                        throw new SAXException(x);
                    }
                    xisRelativeSystemId = "file:/" + encodedPath;
                } else {
                    xisRelativeSystemId = xis.getSystemId();
                }
            } catch (URISyntaxException xe) {
                xisRelativeSystemId = xis.getSystemId();
            }
            th.characters(xisRelativeSystemId.toCharArray(), 0, xisRelativeSystemId.length());
            th.endElement(null, INPUT_SOURCE_URI_TAG, INPUT_SOURCE_URI_TAG);
            
            th.endElement(null, META_TAG, META_TAG);
            th.startElement(null, CONTENT_TAG, CONTENT_TAG, att);
            
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
            th.endElement(null, CONTENT_TAG, CONTENT_TAG);
            th.endElement(null, OPENING_TAG, OPENING_TAG);
        }
        super.endDocument();
    }

}
