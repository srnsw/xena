/*
 * Created on 26/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

public class SourceURIParser {
    
    
    /**
     * Return the relative system Id for a given XenaInputSource. This assumes that 
     * the base path has been set in the meta data wrapper by which all local system
     * id should be resolved from.
     * 
     * TODO: aak - SourceURIParser - better description required please...
     * 
     * @param xis
     * @param pluginManager
     * @return String - the relative path and name of the file relative to the base path set in
     * the meta data wrapper, or, if that is not able to be resolved (ie base path is not set or
     * the XIS URI has nothing to do with the base path) then the entire URI of the XIS.
     * @throws SAXException - In the event that the URI from the XIS cannot be encoded.
     */
    public static String getRelativeSystemId(XenaInputSource xis, PluginManager pluginManager) 
    throws SAXException {
        String xisRelativeSystemId = "";
        try {
            java.net.URI uri = new java.net.URI(xis.getSystemId());
            if (uri.getScheme() != null && "file".equals( uri.getScheme() )) {
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
                if (pluginManager.getMetaDataWrapperManager().getBasePathName() != null) {
                    try {
                        baseDir = new File(pluginManager.getMetaDataWrapperManager().getBasePathName());
                        if (baseDir != null) {
                            relativePath = FileName.relativeTo(baseDir, inputSourceFile);
                        }
                    } catch (IOException iox) {
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
                        relativePath = null;
                    } catch (XenaException xe) {
                        relativePath = null;
                    }
                }
                if (relativePath == null) {
                    relativePath = inputSourceFile.getAbsolutePath();
                }
                String encodedPath = null;
                try {
                    encodedPath = au.gov.naa.digipres.xena.util.UrlEncoder.encode(relativePath);
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
        return xisRelativeSystemId;
    }
    
    /**
     * Return only the filename component of the source uri.
     * Basically everything after the last '/' and '\'.
     * 
     * TODO: aak - SourceURIParser - better description required please...
     * 
     * @param xis
     * @return
     */
    public static String getFileNameComponent(XenaInputSource xis) {
        String systemId = xis.getSystemId();
        
        //we really only want everything after the last '\' or '/'
        // this should really be fixed up. right now i want lunch so this will be quick and dirty.
        // aak - heheh this is poxy. my code is teh sux00rs
        int startOfFileName = systemId.lastIndexOf('/');
        String noSlashFileName = systemId.substring(startOfFileName == -1 ? 0 : startOfFileName);
        startOfFileName = noSlashFileName.lastIndexOf('\\');
        String fileName = noSlashFileName.substring(startOfFileName == -1 ? 0 : startOfFileName);
        return fileName;
        
    }
    

}
