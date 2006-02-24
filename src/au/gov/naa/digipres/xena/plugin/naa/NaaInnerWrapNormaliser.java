package au.gov.naa.digipres.xena.plugin.naa;
// SAX classes.
//JAXP 1.1
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Wrap the XML with NAA approved meta-data.
 *
 * @author Chris Bitmead
 */
public class NaaInnerWrapNormaliser extends XMLFilterImpl {
	static SimpleDateFormat isoDateFormat = new SimpleDateFormat(
		"yyyy-MM-dd'T'HH:mm:ss");

    
	public void startDocument() throws org.xml.sax.SAXException {
	    XMLReader normaliser = (XMLReader)getProperty("http://xena/normaliser");
	    if (normaliser == null) {
	        throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
	    }
	    boolean isBinary = normaliser.getClass().getName().equals("au.gov.naa.digipres.xena.plugin.basic.BinaryToXenaBinaryNormaliser");
	    XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
	    super.startDocument();
	    File outfile = ((File)getProperty("http://xena/file"));
	    
	    if (xis.getFile() != null || outfile != null) {
	        ContentHandler th = getContentHandler();
	        AttributesImpl att = new AttributesImpl();
	        th.startElement(NaaTagNames.PACKAGE_URI, NaaTagNames.PACKAGE,NaaTagNames.PACKAGE_PACKAGE, att);
	        if (outfile != null) {
	            
	            /*
	             * Add the NAA Package wrapper string.
	             */
	            th.startElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META, att);
	            th.startElement(NaaTagNames.NAA_URI, NaaTagNames.WRAPPER, NaaTagNames.NAA_WRAPPER, att);
	            th.characters(NaaTagNames.NAA_PACKAGE.toCharArray(), 0, NaaTagNames.NAA_PACKAGE.toCharArray().length);
	            th.endElement(NaaTagNames.NAA_URI, NaaTagNames.WRAPPER, NaaTagNames.NAA_WRAPPER);
	            th.endElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META);
	            
	            
	            /*
	             * Add the identifier for the package.
	             * 
	             * TODO: aak 2005-09-15 [appears fixed 2005-10-05] FIX ME! This is supposed to obtain the ID, 
	             * possibly from the filename(?). Should use the XIS.
	             * perhaps, if we ask nicely, the filenamer will return the last generated name for this XIS.
	             *  or, at least, the map of generated names for each xis, and we can get the latest name for our particular xis.
	             * that makes all sorts of assumptions that could prove incorrect. however... we shall perservere.
	             */
	            th.startElement(NaaTagNames.PACKAGE_URI,NaaTagNames.META, NaaTagNames.PACKAGE_META, att);
	            th.startElement(NaaTagNames.DC_URI, NaaTagNames.IDENTIFIER,NaaTagNames.DCIDENTIFIER, att);
	            Map<String, List<String>>namerNameMap = FileNamerManager.singleton().getActiveFileNamer().getNameMap();
	            String lastGeneratedName = "Unknown_ID";
	            if (namerNameMap.containsKey(xis.getSystemId())) {
	                List<String> nameList = namerNameMap.get(xis.getSystemId());
	                lastGeneratedName = nameList.get(nameList.size() - 1);
	            }
	            char[] id = lastGeneratedName.toCharArray();
	            th.characters(id, 0, id.length);
	            th.endElement(NaaTagNames.DC_URI, NaaTagNames.IDENTIFIER, NaaTagNames.DCIDENTIFIER);
	            th.endElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META);
	            
	            /*
	             * Add the date that the package was created by Xena.
	             */
	            th.startElement(NaaTagNames.PACKAGE_URI,NaaTagNames.META,"package:meta", att);
	            th.startElement(NaaTagNames.DCTERMS_URI, NaaTagNames.CREATED, NaaTagNames.DCCREATED, att);
	            char[] sDate = isoDateFormat.format(new java.util.Date(System.currentTimeMillis())).toCharArray();
	            th.characters(sDate, 0, sDate.length);
	            th.endElement(NaaTagNames.DCTERMS_URI, NaaTagNames.CREATED, NaaTagNames.DCCREATED);
	            th.endElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META);
	        }
	        
	        /*
	         * Add out data sources meta information.
	         */
	        th.startElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META, att);
	        th.startElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCES, NaaTagNames.NAA_DATASOURCES,att);
	        
	        /*
	         * This is indented to indicate this block of code is responsible for doing the datasources.
	         * TODO: The following code should be commented to indicate what meta data is being written.
	         */
	        {
	            List<XenaInputSource> xenaInputSourceList = new ArrayList<XenaInputSource>();
	            if (xis instanceof MultiInputSource) {
	                Iterator it = ((MultiInputSource)xis).getSystemIds().iterator();
	                while (it.hasNext()) {
	                    String url = (String)it.next();
	                    xenaInputSourceList.add(new XenaInputSource(url, null));
	                }
	            } else {
	                xenaInputSourceList.add(xis);
	            }
	            Iterator it = xenaInputSourceList.iterator();
	            while (it.hasNext()) {
	                XenaInputSource source = (XenaInputSource)it.next();
	                th.startElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCE, NaaTagNames.NAA_DATASOURCE,att);
	                XenaInputSource relsource = null;
	                //notout
	                //System.out.println("About to try get our path...");
	                try {
	                    java.net.URI uri = new java.net.URI(source.getSystemId());
	                    if (uri.getScheme().equals("file")) {
	                        File file = new File(uri);
	                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	                        char[] lastModStr = sdf.format(new Date(file.lastModified())).toCharArray();
	                        th.startElement(NaaTagNames.NAA_URI, NaaTagNames.LASTMODIFIED,NaaTagNames.NAA_LASTMODIFIED,att);
	                        th.characters(lastModStr, 0, lastModStr.length);
	                        th.endElement(NaaTagNames.NAA_URI, NaaTagNames.LASTMODIFIED,NaaTagNames.NAA_LASTMODIFIED);
	                        
	                        //TODO nested try / catch. nasty. can we refactor this somewhat?
	                        // this needs to be done as a matter of urgency...
	                        
	                        
	                        /*
	                         * Get the path location. 
	                         * 
	                         * First off, see if we can get a path from the filter manager, and get a relative path.
	                         * If that doesnt work, try to get a legacy base path, and a relative path from that.
	                         * If still no success, then we set the path to be the full path name.
	                         * 
	                         */
	                        String relativePath = null;
	                        File baseDir;
	                        
	                        if (PluginManager.singleton().getFilterManager().getBasePathName() != null) {
	                            try {
	                                baseDir = new File(PluginManager.singleton().getFilterManager().getBasePathName());
	                                if (baseDir != null) {
	                                    relativePath = FileName.relativeTo(baseDir, file);
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
	                                    relativePath = FileName.relativeTo(baseDir, file);
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
	                            relativePath = file.getAbsolutePath();
	                        }
	                        //notout
	                        //System.out.println("Path: " + realativePath);
	                        String encodedPath = null;
	                        try {
	                            encodedPath = au.gov.naa.digipres.xena.helper.UrlEncoder.encode(relativePath);
	                        } catch (UnsupportedEncodingException x) {
	                            throw new SAXException(x);
	                        }
	                        relsource = new XenaInputSource(new java.net.URI("file:/" + encodedPath).toASCIIString(), null);
	                    } else {
	                        relsource = source;
	                    }
	                    //sysout
	                    System.out.println("relsource source:" + relsource.getSystemId());
	                } catch (java.net.URISyntaxException x) {
	                    x.printStackTrace();
	                    // Nothing
	                }
	                
	                //notout
	                //System.out.println("Moving on from path...");
	                th.startElement(NaaTagNames.DC_URI, NaaTagNames.SOURCE, NaaTagNames.DCSOURCE,att);
	                char[] src = relsource.getSystemId().toCharArray();
	                th.characters(src, 0, src.length);
	                th.endElement(NaaTagNames.DC_URI, NaaTagNames.SOURCE, NaaTagNames.DCSOURCE);
	                if (!isBinary) {
	                    List lst = new ArrayList();
	                    lst.add(source.getSystemId());
	                    // TODO: comment by chris bitmead: THIS SHOULD BE CHANGED TO CATER FOR MULTIPLE FILES
	                    // Not sure what you are refering to here Chris mate. Not sure at all.
	                    File file = xis.getUltimateFile();
	                    if (file != null) {
	                        
	                        Map<String, List<String>>namerNameMap = FileNamerManager.singleton().getActiveFileNamer().getNameMap();
	                        String lastGeneratedName = "Unknown_ID";
	                        if (namerNameMap.containsKey(xis.getSystemId())) {
	                            List<String> nameList = namerNameMap.get(xis.getSystemId());
	                            lastGeneratedName = nameList.get(nameList.size() - 1);
	                        }
	                        char[] sourceid = lastGeneratedName.toCharArray();
	                        th.startElement(NaaTagNames.NAA_URI, NaaTagNames.SOURCEID, NaaTagNames.NAA_SOURCEID,att);
	                        th.characters(sourceid, 0, sourceid.length);
	                        th.endElement(NaaTagNames.NAA_URI, NaaTagNames.SOURCEID, NaaTagNames.NAA_SOURCEID);
	                    }
	                }
	                if (isBinary) {
	                    char[] typename = "binary data".toCharArray();
	                    th.startElement(NaaTagNames.NAA_URI, NaaTagNames.TYPE, NaaTagNames.NAA_TYPE,att);
	                    th.characters(typename, 0, typename.length);
	                    th.endElement(NaaTagNames.NAA_URI, NaaTagNames.TYPE, NaaTagNames.NAA_TYPE);
	                }
	                th.endElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCE, NaaTagNames.NAA_DATASOURCE);
	            }
	        }
	        th.endElement(NaaTagNames.NAA_URI, NaaTagNames.DATASOURCES, NaaTagNames.NAA_DATASOURCES);
	        th.endElement(NaaTagNames.PACKAGE_URI, NaaTagNames.META, NaaTagNames.PACKAGE_META);
	        
	        /*
	         * Add our package content.
	         */
	        th.startElement(NaaTagNames.PACKAGE_URI, "content","package:content", att);
	    }
	}

	public void endDocument() throws org.xml.sax.SAXException {
		XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
		File outfile = ((File)getProperty("http://xena/file"));
        /*
         * close our package content.
         */
		if (xis.getFile() != null || outfile != null) {
			ContentHandler th = getContentHandler();
			th.endElement(NaaTagNames.PACKAGE_URI, "content","package:content");
			th.endElement(NaaTagNames.PACKAGE_URI, NaaTagNames.PACKAGE,NaaTagNames.PACKAGE_PACKAGE);
		}
        /*
         * We are all done.
         */
		super.endDocument();
	}

}
