/*
 * Created on 29/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.filenamer;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

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

    public String getName(){
        return "Default Xena file namer";
    }
    
    public String toString() {
        return getName();
    }
    
    public File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
        String id = "00000000";
        if ((destinationDir == null) || (!destinationDir.exists()) || (!destinationDir.isDirectory())) {
            throw new XenaException("Could not create new file because there was an error with the destination directory (" + destinationDir.toString() + ").");
        }
        File newXenaFile = null;
        if (destinationDir != null) {
            id = getId(input, normaliser.toString());
            String fileName = id + "." + FileNamerManager.DEFAULT_EXTENSION;
            newXenaFile = new File(destinationDir, fileName);
            int i = 1;
            DecimalFormat idFormatter = new DecimalFormat("0000");
            while (newXenaFile.exists()) {
                fileName = id + idFormatter.format(i) + "." + FileNamerManager.DEFAULT_EXTENSION;
                newXenaFile = new File(destinationDir, fileName);
                i++;
            }
        }
        return newXenaFile;
    }

    private String getId(XenaInputSource input, String normaliserName) {
        // generate the id for this file.
        String systemId = input.getSystemId();
        
        //we really only want everything after the last '\' or '/'
        // this should really be fixed up. right now i want lunch so this will be quick and dirty.
        // aak - heheh this is poxy. my code is teh sux00rs
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
        return newName;
    }
    
    public FileFilter makeFileFilter(String extension) {
        return null;
    }

}
