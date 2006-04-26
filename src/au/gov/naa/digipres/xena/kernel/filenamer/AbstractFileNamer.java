package au.gov.naa.digipres.xena.kernel.filenamer;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

/**
 *
 * @see FileNamerManager
 * @author aak
 */
public abstract class AbstractFileNamer {
    
    // Match the XenaInput source to generated filenames.
    protected static Map<String, List<String>>nameMap = new HashMap<String, List<String>>();

    protected boolean overwrite;
    
    protected FileNamerManager fileNamerManager;
    
    public AbstractFileNamer(){
    }

    public File makeNewXenaFile(XenaInputSource xis, AbstractNormaliser normaliser) throws XenaException {
        return makeNewXenaFile(xis, normaliser, fileNamerManager.getDestinationDir());
    }
    
    public abstract String getName();
    
    public abstract File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException;
    
	public abstract FileFilter makeFileFilter(String extension);

    /**
     * @return Returns the overwriteOldFiles.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * @param overwriteOldFiles The overwriteOldFiles to set.
     */
    public void setOverwrite(boolean overwriteOldFiles) {
        this.overwrite = overwriteOldFiles;
    }

    /**
     * @return Returns the nameMap.
     */
    public Map<String, List<String>> getNameMap() {
        return AbstractFileNamer.nameMap;
    }

    /**
     * @param nameMap The nameMap to set.
     */
    public void setNameMap(Map<String, List<String>> nameMap) {
        AbstractFileNamer.nameMap = nameMap;
    }

    /**
     * @return Returns the fileNamerManager.
     */
    public FileNamerManager getFileNamerManager() {
        return fileNamerManager;
    }

    /**
     * @param fileNamerManager The new value to set fileNamerManager to.
     */
    public void setFileNamerManager(FileNamerManager fileNamerManager) {
        this.fileNamerManager = fileNamerManager;
    }
    
}
