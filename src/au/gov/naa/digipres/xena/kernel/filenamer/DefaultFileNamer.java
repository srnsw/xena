/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.filenamer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * @author andrek24 created 29/09/2005 xena Short desc of class:
 */
public class DefaultFileNamer extends FileNamer {

    private Map<String, String> generatedIdToSystemIdList = new HashMap<String, String>();

    /**
     * 
     */
    public DefaultFileNamer() {
        super();
    }

    /**
     * @param keepHistoryFile
     * @param historyFile
     */
    public DefaultFileNamer(boolean keepHistoryFile, File historyFile) {
        super(keepHistoryFile, historyFile);
    }
    
    public DefaultFileNamer(boolean overwrite, boolean keepHistoryFile, File historyFile) {
        this.overwrite = overwrite;
        this.keepHistoryFile = keepHistoryFile;
        this.historyFile = historyFile;
    }

    public String toString(){
        return "Default Xena file namer.";
    }
    
    public File makeNewXenaFile(XMLReader normaliser, XenaInputSource input, String extension) throws XenaException {

        File newDir = null;
        if (extension.equals(FileNamer.XENA_DEFAULT_EXTENSION)) {
            newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.DESTINATION_DIR_STRING);
        } else {
            if (extension.equals(FileNamer.XENA_CONFIG_EXTENSION)) {
                newDir = LegacyXenaCode.getBaseDirectory(NormaliserManager.CONFIG_DIR_STRING);
            } else {
                throw new XenaException("Unrecognised extension: " + extension + ". Could not derive destination dir.");
            }
        }
        return makeNewXenaFile(normaliser, input, extension, newDir);
    }
    
    
    
    public File makeNewXenaFile(XMLReader normaliser, XenaInputSource input,
            String extension, File destinationDir) throws XenaException {
        String id = "00000000";
        if ((destinationDir == null) || (!destinationDir.exists()) || (!destinationDir.isDirectory())) {
            throw new XenaException(
                    "Could not create new file because there was an error with the destination directory (" + destinationDir.toString() + ").");
        }
        File newNaaFile = null;
        if (destinationDir != null) {
            id = getId(input, normaliser.toString());
            newNaaFile = new File(destinationDir, id + "." + extension);
        }
        return newNaaFile;
    }

    
    
    private String getId(XenaInputSource input, String normaliserName) {
        // generate the name for this file.
       
        
        String systemId = input.getSystemId();
        
        //we really only want everything after the last '\' or '/'
        // thus....
        int startOfFileName = systemId.lastIndexOf("\\");
        if (startOfFileName == -1) {
            startOfFileName = systemId.lastIndexOf("/");
        }
        if (startOfFileName == -1) {
            startOfFileName = 0;
        }
        String fileName = systemId.substring(startOfFileName);
        
        String newName = fileName + "_" + normaliserName;

        if (generatedIdToSystemIdList.containsKey(newName)) {
            if (overwrite == false) {
                int i = 0;
                newName = String.format("%s_%s_%2d", fileName, normaliserName, i);
                while (generatedIdToSystemIdList.containsKey(newName)) {
                    i++;
                    newName = String.format("%s_%s_%2d", fileName, normaliserName, i);
                }
            }
        }
        // and the history file?
        if (keepHistoryFile) {
            // add the entry to the history file.
            if (historyFile != null) {
                if (!historyFile.exists()) {
                    // if it doesnt exist, try and create it.
                    try {
                        historyFile.createNewFile();
                    } catch (IOException e) {
                        System.out.println("Could not create history file for some reason.");
                    }   
                }
                if (!historyFile.canWrite()) {
                    //if we cant write, forget about it...
                    System.out.print("Can not write to history file for some reason.");
                    System.out.print("It may be locked or you may not have permission to write to it.");
                    System.out.println();
                }else if (historyFile.exists()) {
                    // hooray! it exists, we can write to it, all should be good!
                    try {
                        FileWriter fw = new FileWriter(historyFile, true);
                        fw.append(newName);
                        fw.append("\t");
                        fw.append(systemId);
                    } catch (Exception e) {
                        // or not....
                        System.out.println("There was a problem adding the item to the history file.");
                    }
                }
            } else {
                System.out.println("There was a problem opening the history file for writing.");
            }
        }
        
        // now add it to the nameMap that all file namers must keep up to date...
        addToNameMap(input, newName);
        return newName;
    }

    
    
    
    public FileFilter makeFileFilter(String extension) {

        return null;
    }

}
