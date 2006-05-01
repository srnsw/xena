/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Date;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.SourceURIParser;
import au.gov.naa.digipres.xena.util.TagContentFinder;

public class OrgXMetaDataWrapper extends AbstractMetaDataWrapper {

    public static final String ORGX_OPENING_TAG = "orgx";    
    public static final String ORGX_META_TAG = "meta";    
    public static final String ORGX_DEPARTMENT_TAG = "department";    
    public static final String ORGX_USER_TAG = "user_name";    
    public static final String ORGX_INPUT_NAME_TAG = "input_name";    
    public static final String ORGX_CONTENT_TAG = "record_data";    
    public static final String ORGX_ID_TAG = "orgx_id";    
    public static final String ORGX_TIMESTAMP_TAG = "timestamp";    
    public static final String ORGX_HEADER_TAG = "orgx_header";    

    private static final String DEFAULT_USER = "unknown user";
    private static final String DEFAULT_DEPARTMENT = "unknown department";
    private static final String DEFAULT_FILENAME = "unknown_filename";
    private InfoProvider myInfoProvider = null;
    
    /**
     * @return Returns the myInfoProvider.
     */
    public InfoProvider getMyInfoProvider() {
        if (myInfoProvider == null)
        {
        	PropertiesManager propManager = this.getMetaDataWrapperManager().getPluginManager().getPropertiesManager();
        	myInfoProvider = new PropertiesInfoProvider(propManager);
        }
        return myInfoProvider;
    }

    /**
     * @param myInfoProvider The new value to set myInfoProvider to.
     */
    public void setMyInfoProvider(InfoProvider myInfoProvider) {
        this.myInfoProvider = myInfoProvider;
    }
    
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
        return TagContentFinder.getTagContents(input, ORGX_INPUT_NAME_TAG);
    }

    
    @Override
    public void startDocument() throws SAXException {

        String departmentName = (getMyInfoProvider() != null ? getMyInfoProvider().getDepartmentName() : DEFAULT_DEPARTMENT);
        String userName = (getMyInfoProvider() != null ? getMyInfoProvider().getUserName() : DEFAULT_USER);
        
        boolean insertTimestamp = (getMyInfoProvider() != null ? getMyInfoProvider().isInsertTimestamp() : false);
        File headerFile = (getMyInfoProvider() != null ? getMyInfoProvider().getHeaderFile() : null);
        
        String fileName = "";
        try {
            XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
            if (xis != null) {
                fileName = SourceURIParser.getRelativeSystemId(xis, metaDataWrapperManager.getPluginManager());
            }
        } catch (SAXException saxe) {
            fileName = "Unknown";
        }
        
        super.startDocument();
        ContentHandler th = getContentHandler();
        AttributesImpl att = new AttributesImpl();
        th.startElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG, att);
        th.startElement(null, ORGX_META_TAG, ORGX_META_TAG, att);
        
        
        // Header
        try
        {
	        if (headerFile != null && headerFile.exists() && headerFile.isFile())
	        {
	        	StringBuffer headerBuffer = new StringBuffer();
	        	BufferedReader reader = new BufferedReader(new FileReader(headerFile));
	        	String line = reader.readLine();
	        	while (line != null)
	        	{
	        		headerBuffer.append(line);
	        		line = reader.readLine();
	        	}
	        	
	            th.startElement(null, ORGX_HEADER_TAG, ORGX_HEADER_TAG, att);
	            th.characters(headerBuffer.toString().toCharArray(), 0, headerBuffer.length());
	            th.endElement(null, ORGX_HEADER_TAG, ORGX_HEADER_TAG);        
	        }
        }
        catch (IOException iex)
        {
        	throw new SAXException(iex);
        }
        
        // Timestamp
        if (insertTimestamp)
        {
        	String dateStr = new Date().toString();
            th.startElement(null, ORGX_TIMESTAMP_TAG, ORGX_TIMESTAMP_TAG, att);
            th.characters(dateStr.toCharArray(), 0, dateStr.length());
            th.endElement(null, ORGX_TIMESTAMP_TAG, ORGX_TIMESTAMP_TAG);        
        }

        // department name
        th.startElement(null, ORGX_DEPARTMENT_TAG, ORGX_DEPARTMENT_TAG, att);
        th.characters(departmentName.toCharArray(), 0, departmentName.toCharArray().length);
        th.endElement(null, ORGX_DEPARTMENT_TAG, ORGX_DEPARTMENT_TAG);        
        
        // user name
        th.startElement(null, ORGX_USER_TAG, ORGX_USER_TAG, att);
        th.characters(userName.toCharArray(), 0, userName.toCharArray().length);
        th.endElement(null, ORGX_USER_TAG, ORGX_USER_TAG);
        
        // input name
        th.startElement(null, ORGX_INPUT_NAME_TAG, ORGX_INPUT_NAME_TAG, att);
        th.characters(fileName.toCharArray(), 0, fileName.toCharArray().length);
        th.endElement(null, ORGX_INPUT_NAME_TAG, ORGX_INPUT_NAME_TAG);
        
        // org x ID
        th.startElement(null, ORGX_ID_TAG, ORGX_ID_TAG, att);
        String orgx_id = fileName + "_" + departmentName + "_" + userName + "_";
        th.characters(orgx_id.toCharArray(), 0, orgx_id.toCharArray().length);
        th.endElement(null, ORGX_ID_TAG, ORGX_ID_TAG);
        
        
        
        th.endElement(null, ORGX_META_TAG, ORGX_META_TAG);
        th.startElement(null, ORGX_CONTENT_TAG, ORGX_CONTENT_TAG, att);
            
    }

    @Override
    public void endDocument() throws org.xml.sax.SAXException {
        ContentHandler th = getContentHandler();
        th.endElement(null, ORGX_CONTENT_TAG, ORGX_CONTENT_TAG);
        th.endElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG);
        super.endDocument();
    }
    
    
    
}
