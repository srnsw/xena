package au.gov.naa.digipres.xena.kernel.filenamer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * The way that Xena decides how to name the output files is determined by a
 * FileNamer. It is very easy for third parties to write their own FileNamers
 * and the user can choose which one to use from the Properties menu item.
 * <cough>bullshit</cough>
 *
 * @see FileNamer
 * @author Chris Bitmead
 */
public class FileNamerManager implements LoadManager {
	/**
	 * String className -> FileNamer
	 */
	protected Map<String, FileNamer> namers = new HashMap<String, FileNamer>();

	public final static String FILE_NAMER_PREF = "fileNamer";

    
//	static FileNamerManager theSingleton = new FileNamerManager();
//	
//	public static FileNamerManager singleton() {
//	    return theSingleton;
//	}

    private PluginManager pluginManager;
    
    private FileNamer activeFileNamer;
    
    
	public FileNamerManager(PluginManager pluginManager) {
	    this.pluginManager = pluginManager;
        FileNamer defaultNamer = new DefaultFileNamer(false, false, null);
        activeFileNamer = defaultNamer;
        namers.put(defaultNamer.getClass().getName(), defaultNamer);
	}

	/**
     * @return Returns the pluginManager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * @param pluginManager The new value to set pluginManager to.
     */
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public boolean load(JarPreferences preferences) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(preferences);
			List transes = loader.loadInstances("fileNamers");
			Iterator it = transes.iterator();

			while (it.hasNext()) {
				FileNamer namer = (FileNamer)it.next();
				namers.put(namer.getClass().getName(), namer);
                
                if ( activeFileNamer.getClass().equals(DefaultFileNamer.class) ) {
                    activeFileNamer = namer;
                }
            }
			return!transes.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	/**
	 * Get all the available File Namers.
	 * @return Collection of File Namers
	 */
	public Collection<FileNamer> getFileNamers() {
		return namers.values();
	}

    /**
     * Get a list of the name of the loaded file namers
     * @return Collection of File Namer names 
     */
    public Collection<String> getListOfNamers() {
        return namers.keySet();
    }
    
	/**
	 * Find a FileNamer object given its class name.
	 * @param name class name
	 * @return FileNamer
	 */
	public FileNamer lookupByClassName(String name) {
		return namers.get(name);
	}

    /**
     * return the Active file namer.
     * @return FileNamer
     */
    public FileNamer getActiveFileNamer(){
        return activeFileNamer;
    }
    
    /**
     * Set active filenamer to be specified filenamer. return true on success.
     * @param fileNamer
     * @return boolean
     */
    public boolean setActiveFileNamer(FileNamer fileNamer){
        if (fileNamer != null) {
            activeFileNamer = fileNamer;
            return true;
        }
        return false;
    }

    /**
     * Set active filenamer by name. return true on success.
     * @param fileNamer
     * @return boolean
     */
    public boolean setActiveFileNamer(String name) {
        if (namers.get(name) != null){
            activeFileNamer = namers.get(name);
            return true;
        }
        return false;
    }
    
    
	/**
     * TODO: This should really be removed. The 'prefs' should be handled by the GUI
     * or whatever app needs them...
	 * Get the FileNamer that has currently been configured by the user.
	 * @return FileNamer
	 */
	public FileNamer getFileNamerFromPrefs() {
		JarPreferences root = (JarPreferences)JarPreferences.userNodeForPackage(NormaliserManager.class);
		String fileNamerName = root.get(FILE_NAMER_PREF, "");
		if (!fileNamerName.equals("")) {
			FileNamer en = namers.get(fileNamerName);
			return en;
		}
		return null;
	}

	public void complete() {}
}
