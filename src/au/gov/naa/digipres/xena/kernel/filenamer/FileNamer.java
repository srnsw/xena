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

/**
 *
 * @see FileNamerManager
 * @author aak
 */
public abstract class FileNamer {
    
    public static final String XENA_DEFAULT_EXTENSION = "xena";
    public static final String XENA_CONFIG_EXTENSION = "xcfg";
    
    public static final String SEPERATOR = "\t";
    
    
    // Match the XenaInput source to generated filenames.
    protected static Map<String, List<String>>nameMap = new HashMap<String, List<String>>();

    protected boolean overwrite;
    
    protected boolean keepHistoryFile;
    protected File historyFile;
    
    public FileNamer(){
        this.keepHistoryFile = false;
        this.historyFile = null;
    }
    
    public FileNamer(boolean keepHistoryFile, File historyFile) {
        this.historyFile = historyFile;
        this.keepHistoryFile = keepHistoryFile;
    }
    
    public abstract File makeNewXenaFile(XMLReader normaliser, XenaInputSource input, String extension) throws XenaException;
    
    public abstract File makeNewXenaFile(XMLReader normaliser, XenaInputSource input, String extension, File destinationDir) throws XenaException;
    
	public abstract FileFilter makeFileFilter(String extension);

    /**
     * @return Returns the historyFile.
     */
    public File getHistoryFile() {
        return historyFile;
    }

    /**
     * @param historyFile The historyFile to set.
     */
    public void setHistoryFile(File historyFile) {
        this.historyFile = historyFile;
    }

    /**
     * Create new history file ready for writing!
     * @param historyFileName the name of the history file...
     */
    public void createHistoryFile(String historyFileName) throws IOException {
        historyFile = new File(historyFileName);
        historyFile.createNewFile();
    }
    
    
    /**
     * @param historyFileName The name of the historyFile to create.
     */
    public void setHistoryFile(String historyFileName) {
        this.historyFile = new File(historyFileName);
    }

    /**
     * @return Returns the keepHistoryFile.
     */
    public boolean isKeepHistoryFile() {
        return keepHistoryFile;
    }

    /**
     * @param keepHistoryFile The keepHistoryFile to set.
     */
    public void setKeepHistoryFile(boolean keepHistoryFile) {
        this.keepHistoryFile = keepHistoryFile;
    }

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
        return FileNamer.nameMap;
    }

    /**
     * @param nameMap The nameMap to set.
     */
    public void setNameMap(Map<String, List<String>> nameMap) {
        FileNamer.nameMap = nameMap;
    }
    
    protected void addToNameMap(XenaInputSource xis, String newName){
        List<String> nameList = null;
        if (FileNamer.nameMap.containsKey(xis.getSystemId())){
            nameList = FileNamer.nameMap.get(xis.getSystemId());
        } else {
            nameList = new ArrayList<String>();
        }
        nameList.add(newName);
        FileNamer.nameMap.put(xis.getSystemId(), nameList);
    }
    
}
