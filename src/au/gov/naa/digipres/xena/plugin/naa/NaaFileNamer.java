package au.gov.naa.digipres.xena.plugin.naa;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

/**
 * Generate a file name for the output file according to NAA policy.
 *
 * @author Chris Bitmead
 */
public class NaaFileNamer extends AbstractFileNamer {

    
    public static String NAA_FILE_NAMER = "NAA File Namer";
    
    private static long timeSeed;
    static {
        timeSeed = System.currentTimeMillis() / 1000;
        timeSeed += timeSeed % 2;
    }
    
	public NaaFileNamer() {
	}


    public String toString() {
        return this.getName();
    }
    
    public String getName() {
        return NAA_FILE_NAMER;
    }


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

	public FileFilter makeFileFilter() {
	    return FileNamerManager.DEFAULT_FILE_FILTER;
	}
    
    
}
