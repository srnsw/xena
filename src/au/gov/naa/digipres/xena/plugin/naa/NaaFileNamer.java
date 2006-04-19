package au.gov.naa.digipres.xena.plugin.naa;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.SimpleFileFilter;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * Generate a file name for the output file according to NAA policy.
 *
 * @author Chris Bitmead
 */
public class NaaFileNamer extends AbstractFileNamer {

    private static final String SEP = "\t";
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    private static long timeSeed;
    static {
        timeSeed = System.currentTimeMillis() / 1000;
        timeSeed += timeSeed % 2;
    }
    
	public NaaFileNamer() {
	}


    public String toString() {
        return "NAA File Namer";
    }
    
    
//    /**
//     * Make the filename for the new XenaOutputStream.
//     * This is a fully qualified filename, based on the folders that were specified by the normaliser manager.
//     * 
//     * THIS IS RETARDED. WE GET THE DIRECTORY BASED ON THE FILE EXTENSION.
//     * THIS COMMENT IS _MEANT_ TO BE ALL CAPS.
//     * 
//     * Ultimately, this should not be the case.
//     * 
//     */
//    public File makeNewXenaFile(XMLReader normaliser, XenaInputSource input) 
//    throws XenaException {
//            File newDir = null;
////            if (extension.equals(FileNamer.XENA_DEFAULT_EXTENSION)) {
////                newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.DESTINATION_DIR_STRING);
////            } else {
////                if (extension.equals(FileNamer.XENA_CONFIG_EXTENSION)) {
////                    newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.CONFIG_DIR_STRING);
////                } else {
////                    throw new XenaException("Unknown extension: " + extension);
////                }
////            }
//            
//            newDir = fileNamerManager.getDestinationDir();
//            return makeNewXenaFile(normaliser, input, extension, newDir);
//    }

    /**
     * Make the filename for the new XenaOutputStream.
     * This is a fully qualified filename, based on the folders specified.
     * 
     * 
     */
    public File makeNewXenaFile(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) 
    throws XenaException {
        
        // lets sort this function out.
        String id = null;
        
        String systemId = xis.getSystemId();
        
        // Time to generate file name.
        
        //create a list of URLs to feed to our get ID method
        // (in the case of non-multi source the list just has 1 element!)
        List<String> inputSourceUrls = new ArrayList<String>();
        
        if (xis instanceof MultiInputSource) {
            MultiInputSource minput = (MultiInputSource)xis;
            inputSourceUrls.addAll(minput.getSystemIds());
        } else {
            inputSourceUrls.add(systemId);
        }
        
        id = getId(inputSourceUrls);
        
        
        assert (id.length() == 16);
        char lc = id.charAt(15);
        char[] chs = {lc};
        int last = Integer.parseInt(new String(chs), 16);
        assert ((last % 2) == 0);
        id = id.substring(0, 15) + Integer.toString(last + 1, 16);
        
        
        // we ALWAYS add a new id to our name map - so we can look it up again later if required.
        addToNameMap(xis, id);
        
        File newXenaFile = new File(destinationDir, id + "." + FileNamerManager.DEFAULT_EXTENSION);
        return newXenaFile;
        
    }

    
    /**
     * Here is where we actually create an ID.
     * Finally!
     * It appears we just use a crc of the fully qualified filename (a uri if you will.)
     * @param urls
     * @return
     */
	private synchronized static String getId(List urls) {
		long idNumber = 0L;
		Iterator it = urls.iterator();
		while (it.hasNext()) {
			String url = (String)it.next();
            // XOR the current crc32 with the next list item. 
			CRC32 crc32 = new CRC32();
            crc32.update(url.getBytes());
            idNumber ^= crc32.getValue();                
		}
		String crcs = Long.toHexString(idNumber);
		while (crcs.length() < 8) {
			crcs = "0" + crcs;
		}
		assert ((timeSeed % 2) == 0);

		String times = Long.toHexString(timeSeed);
		timeSeed += 2;
		while (times.length() < 8) {
			times = "0" + times;
		}
		String rtn = crcs + times;
		assert (rtn.length() == 16);
		return rtn;
	}

	public FileFilter makeFileFilter(String extension) {
		return new SimpleFileFilter(extension);
	}
    
    
}
