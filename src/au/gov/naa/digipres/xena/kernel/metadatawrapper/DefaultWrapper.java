/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.SourceURIParser;
import au.gov.naa.digipres.xena.util.TagContentFinder;

public class DefaultWrapper extends AbstractMetaDataWrapper {

    public static final String OPENING_TAG = "xena";
    public static final String META_TAG = "meta_data";
    public static final String CONTENT_TAG = "content";
    public static final String META_DATA_WRAPPER_NAME_TAG = "meta_data_wrapper_name";
    public static final String NORMALISER_NAME_TAG = "normaliser_name";
    public static final String INPUT_SOURCE_URI_TAG = "input_source_uri";
    
    private final static String DEFAULTWRAPPER = "Default Package Wrapper";
    
    public String getName() {
        return DEFAULTWRAPPER;
    }
    
    public String toString() {
        return "Xena Default XML Wrapper";
    }

    public String getOpeningTag() {
        return OPENING_TAG;
    }
    
    public String getSourceId(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, INPUT_SOURCE_URI_TAG);
    }
    
    public String getSourceName(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, INPUT_SOURCE_URI_TAG);
    }
    
    public void startDocument() throws SAXException {
//        try {
            XMLReader normaliser = (XMLReader)getProperty("http://xena/normaliser");
            if (normaliser == null) {
                throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
            }
            
            XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
            super.startDocument();
            
            //File outfile = ((File)getProperty("http://xena/file"));
            //if (outfile == null)  {
            //    throw new XenaException("Output file was null!");
            //}
            //if (xis.getFile() == null) {
            //    throw new XenaException("XIS input file was null!");
            //}
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

            //TODO: aak - defaultWrapper - This throws a SAXException - is that really necessary?!?
            String xisRelativeSystemId = SourceURIParser.getRelativeSystemId(xis, metaDataWrapperManager.getPluginManager());
            th.characters(xisRelativeSystemId.toCharArray(), 0, xisRelativeSystemId.length());
            
            th.endElement(null, INPUT_SOURCE_URI_TAG, INPUT_SOURCE_URI_TAG);
            
            th.endElement(null, META_TAG, META_TAG);
            th.startElement(null, CONTENT_TAG, CONTENT_TAG, att);
            
//        } catch (XenaException x) {
//            throw new SAXException(x);
//        }
    }

    public void endDocument() throws org.xml.sax.SAXException {
        /*
         * THIS DOESNT WORK FOR EMBEDDED OBJECTS! Not sure why it was here at all really...
         */
//        XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
//        File outfile = ((File)getProperty("http://xena/file"));
//        //int level = ((Integer)getProperty("http://xena/level"));
//        if (xis.getFile() != null || outfile != null) {
//            ContentHandler th = getContentHandler();
//            th.endElement(null, CONTENT_TAG, CONTENT_TAG);
//            th.endElement(null, OPENING_TAG, OPENING_TAG);
//        }

        ContentHandler th = getContentHandler();
        th.endElement(null, CONTENT_TAG, CONTENT_TAG);
        th.endElement(null, OPENING_TAG, OPENING_TAG);
        super.endDocument();
    }

}
