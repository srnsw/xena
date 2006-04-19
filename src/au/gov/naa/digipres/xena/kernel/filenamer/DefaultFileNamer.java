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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * @author andrek24 created 29/09/2005 xena Short desc of class:
 */
public class DefaultFileNamer extends AbstractFileNamer {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

    private Map<String, String> generatedIdToSystemIdList = new HashMap<String, String>();

    /**
     * 
     */
    public DefaultFileNamer() {
        super();
    }

    public String toString(){
        return "Default Xena file namer";
    }
    
    public File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
        String id = "00000000";
        if ((destinationDir == null) || (!destinationDir.exists()) || (!destinationDir.isDirectory())) {
            throw new XenaException("Could not create new file because there was an error with the destination directory (" + destinationDir.toString() + ").");
        }
        File newXenaFile = null;
        if (destinationDir != null) {
            id = getId(input, normaliser.toString());
            newXenaFile = new File(destinationDir, id + "." + FileNamerManager.DEFAULT_EXTENSION);
        }
        return newXenaFile;
    }

    private String getId(XenaInputSource input, String normaliserName) {
        // generate the name for this file.
       
        
        String systemId = input.getSystemId();

        //we really only want everything after the last '\' or '/'
        // this should really be fixed up. right now i want lunch so this will be quick and dirty.
        // heheh this is poxy. my code is teh sux00rs
        int startOfFileName = systemId.lastIndexOf('/');
        String noSlashFileName = systemId.substring(startOfFileName == -1 ? 0 : startOfFileName);
        startOfFileName = noSlashFileName.lastIndexOf('\\');
        String fileName = noSlashFileName.substring(startOfFileName == -1 ? 0 : startOfFileName);
               
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
        // now add it to the nameMap that all file namers must keep up to date...
        addToNameMap(input, newName);
        return newName;
    }
    
    public FileFilter makeFileFilter(String extension) {
        return null;
    }

}
