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
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * Generate a file name for the output file according to NAA policy.
 *
 * @author Chris Bitmead
 */
public class NaaFileNamer extends FileNamer {

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
    
    
    /**
     * Make the filename for the new XenaOutputStream.
     * This is a fully qualified filename, based on the folders that were specified by the normaliser manager.
     * 
     * THIS IS RETARDED. WE GET THE DIRECTORY BASED ON THE FILE EXTENSION.
     * THIS COMMENT IS _MEANT_ TO BE ALL CAPS.
     * 
     * Ultimately, this should not be the case.
     * 
     */
    public File makeNewXenaFile(XMLReader normaliser, XenaInputSource input, String extension) 
    throws XenaException {
            File newDir = null;
            if (extension.equals(FileNamer.XENA_DEFAULT_EXTENSION)) {
                newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.DESTINATION_DIR_STRING);
            } else {
                if (extension.equals(FileNamer.XENA_CONFIG_EXTENSION)) {
                    newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.CONFIG_DIR_STRING);
                } else {
                    throw new XenaException("Unknown extension: " + extension);
                }
            }
            return makeNewXenaFile(normaliser, input, extension, newDir);

            /*
            File newNaaFile = null;
            if (newDir != null) {
                boolean binary = (normaliser != null && normaliser.getClass().getName().equals("au.gov.naa.digipres.xena.plugin.basic.BinaryToXenaBinaryNormaliser"));
                String id = findOrCreateBinaryId(input, binary, NormaliserManager.singleton().getBaseDirectory(NormaliserManager.DESTINATION_DIR_STRING));
                input.setOutputName(id + "." + extension);
                newNaaFile = new File(newDir, id + "." + extension);
            }
            return newNaaFile;
            */
    }

    /**
     * Make the filename for the new XenaOutputStream.
     * This is a fully qualified filename, based on the folders specified.
     * 
     * 
     */
    public File makeNewXenaFile(XMLReader normaliser, XenaInputSource input, String extension, File destinationDir) 
    throws XenaException {
        try {
            
            boolean binary = normaliser != null && normaliser.getClass().getName().equals("au.gov.naa.digipres.xena.plugin.basic.BinaryToXenaBinaryNormaliser");
            String id = findOrCreateBinaryId(input, binary, destinationDir);
            
            // we ALWAYS add a new id to our name map - so we can look it up again later if required.
            addToNameMap(input, id);
            
            File newXenaFile = new File(destinationDir, id + "." + extension);
            return newXenaFile;
        } catch (IOException ex) {
            throw new XenaException(ex);
        }
    }
    
    
    
    /**
     * 
     * @param input
     * @return
     * @throws IOException
     * @throws XenaException
     */
	private String findOrCreateBinaryId(XenaInputSource input, boolean binary, File historyFile)
    throws IOException, XenaException {
        //lets sort this function out.
        // if we are not over writing, then we always need a new file name...
        String id = null;
        
        String systemId = input.getSystemId();
        // IF we are overwriting, then first check our nameMap for the XIS system Id.
        // Then, if we are keeping a history file, *and* overwriting,
        // check to see if our XIS id is in the history file.
        if (overwrite) {
            //first off, lets see if our xis (or any of it's components) is already in the name map...
            if (nameMap.containsKey(systemId)){
                // if so, get the LATEST entry for this systemId...
                List<String> ids = nameMap.get(systemId);
                return (ids.get(ids.size() - 1));
            }
            //okay, not in the nameMap. let us see if it is in the file (provided that history is true)
            if (keepHistoryFile){
                Map<String, List<String>> historyMap = loadHistory();
                if (historyMap != null) {
                    if (historyMap.containsKey(systemId)) {
                        List<String> ids = historyMap.get(systemId);
                        return ids.get(ids.size() - 1);
                    }
                }   
            }   
        }
        
        // Okay so we are not overwriting, OR we couldnt find our file name yet.
        // Time to generate a new one.
        
        //create a list of URLs to feed to our get ID method
        // (in the case of non-multi source the list just has 1 element!)
        List<String> inputSourceUrls = new ArrayList<String>();
        if (input instanceof MultiInputSource) {
            MultiInputSource minput = (MultiInputSource)input;
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
        
        
        
        if (keepHistoryFile) {
            try {
                addEntryToHistoryFile(inputSourceUrls, id);
            } catch (XenaException e) {
                //this shouldnt result in a complete failure... it is just the history file after all!
                logger.finest("Couldn't write to NAA filenamer history file.");
            }
        }
        
        
		return id;
	}
    
//
//	protected static String findOrCreateId(XenaInputSource input, boolean binary) throws
//		IOException, XenaException {
//		String id = findOrCreateBinaryId(input);
//		if (!binary) {
//			assert (id.length() == 16);
//			char lc = id.charAt(15);
//			char[] chs = {lc};
//			int last = Integer.parseInt(new String(chs), 16);
//			assert ((last % 2) == 0);
//			id = id.substring(0, 15) + Integer.toString(last + 1, 16);
//		}
//		return id;
//	}


//    private void addEntryToNameMap(String systemId, String id) {
//        // add out entry to the name map. this is kinda tricky since we have duplicate keys.
//        // hmmmm.
//        List<String> idList;
//        
//        if (nameMap.containsKey(systemId)){
//            idList = nameMap.get(systemId);
//        } else {
//            idList = new ArrayList<String>();
//        }
//        idList.add(id);
//        nameMap.put(systemId, idList);
//    }

    private synchronized void addEntryToHistoryFile(List<String> systemIds, String id) throws XenaException, IOException {
        // first we check to ensure that we have a valid history file....
        if ((historyFile == null) || (!historyFile.exists()) || (!historyFile.canWrite())) {
            throw new XenaException("Cant write to history file!");
        }
        //now we add the id and the system id. so simple!
        // we add the id, then all the sys id's get put afterwards on the same line.
        FileWriter fw = null;
        try {
            fw = new FileWriter(historyFile, true);
            fw.write(id);
            Iterator it = systemIds.iterator();
            while (it.hasNext()) {
                String url = (String) it.next();
                fw.write(SEP);
                fw.write(url);
            }
            fw.write("\n");
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    
    
//    /**
//     * return a map of system ids to actual ids.
//     *  in the case of a multi input, each sys id is associated with the parent.
//     */
//	private Map<String, String> loadHistory(File historyFile) throws IOException {
//	    Map<String, String> historyMap = new HashMap<String,String>();
//        BufferedReader reader = new BufferedReader(new FileReader(historyFile));
//        String line;
//        while ((line = reader.readLine()) != null){
//            StringTokenizer st = new StringTokenizer(line, SEP);
//            String id = st.nextToken();
//            String source = st.nextToken();
//            historyMap.put(source, id);
//            while (st.hasMoreTokens()) {
//                source  = st.nextToken();
//                historyMap.put(source, id);
//            }
//        }
//        return historyMap;
//    }

    
    
    
    /**
     * return a map of system ids to actual ids.
     *  in the case of a multi input, each sys id is associated with the parent.
     * 
     * Returns a map of xis system id (String) to a list of ids that have been generated for that xis.
     * eg:
     * KEY      Value
     * XIS1     { id1, id2, id3 }
     * XIS2     { id4, id5, id6 }
     * MXIS1    { id7, id8 }
     * MXIS2    { id7, id8 }
     * XIS3     { id9 }
     */
    
	private Map<String, List<String>> loadHistory() throws IOException {
		Map<String, List<String>> rtn = new HashMap<String, List<String>>();
        
        if ( (historyFile != null) && (historyFile.exists()) && (historyFile.isFile()) ) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(historyFile));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    StringTokenizer strignTokenizer = new StringTokenizer(line, SEP);
                    //the first thing we get is an ID.
                    String id = strignTokenizer.nextToken();
                    // then we get one or more (in the case of multi inputs) XIS's.
                    // for each XIS, add it to our return list.
                    
                    while (strignTokenizer.hasMoreTokens()) {
                        String source = strignTokenizer.nextToken();
                        List<String> idList;
                        if (rtn.containsKey(source)) {
                            idList = rtn.get(source);
                        } else {
                            idList = new ArrayList<String>();
                        }
                        idList.add(id);
                        rtn.put(source, idList);
                    }
                }
            } catch (FileNotFoundException ex) {
                // Nothing. No history file found.
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        }
		return rtn;
	}

    
    
//	private synchronized static void storeNameHistory(File newDir, List origUrls, String newName) throws IOException {
//        File history = new File(newDir, HISTORY);
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(history, true);
//            fw.write(newName);
//            Iterator it = origUrls.iterator();
//            while (it.hasNext()) {
//                String url = (String) it.next();
//                fw.write(SEP);
//                fw.write(url);
//            }
//            fw.write("\n");
//        } finally {
//            if (fw != null) {
//                fw.close();
//            }
//        }
//    }

    
    
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
