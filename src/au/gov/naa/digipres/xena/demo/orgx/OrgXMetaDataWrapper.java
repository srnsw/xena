/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.util.TagContentFinder;

public class OrgXMetaDataWrapper extends AbstractMetaDataWrapper {

    public static final String ORGX_OPENING_TAG = "orgx";
    
    public static final String ORGX_META_TAG = "meta";
    
    public static final String DEPARTMENT_TAG = "department";
    
    public static final String USER_TAG = "user_name";
    
    public static final String INPUT_NAME_TAG = "input_name";
    
    public static final String CONTENT_TAG = "content_tag";
    
    public static final String ORGX_ID_TAG = "orgx_id";
    
    @Override
    public String getOpeningTag() {
        return ORGX_OPENING_TAG;
    }

    @Override
    public String getSourceId(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, ORGX_ID_TAG);
    }

    @Override
    public String getSourceName(XenaInputSource input) throws XenaException {
        return TagContentFinder.getTagContents(input, INPUT_NAME_TAG);
    }

    
    
    public void startDocument() throws SAXException {
        XMLReader normaliser = (XMLReader)getProperty("http://xena/normaliser");
        if (normaliser == null) {
            throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
        }
        
        XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
        super.startDocument();
        
        ContentHandler th = getContentHandler();
        AttributesImpl att = new AttributesImpl();
        th.startElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG, att);
        
        
        th.startElement(null, ORGX_META_TAG, ORGX_META_TAG, att);
        
        
        
        
        th.endElement(null, ORGX_META_TAG, ORGX_META_TAG);
        th.startElement(null, CONTENT_TAG, CONTENT_TAG, att);
            
    }

    public void endDocument() throws org.xml.sax.SAXException {

        ContentHandler th = getContentHandler();
        th.endElement(null, CONTENT_TAG, CONTENT_TAG);
        th.endElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG);
        super.endDocument();
    }
    
    
    
}
