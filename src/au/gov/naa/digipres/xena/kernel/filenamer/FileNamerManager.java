package au.gov.naa.digipres.xena.kernel.filenamer;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.plugin.LoadManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * <p>The way that Xena decides how to name the output files is determined by a
 * FileNamer.</p>
 * 
 * <p>It is envisioned that the naming of Xena output files will be institution specific,
 * and as such the FileNamers can easily be made by extending the AbstractFileNamer object.</p>
 * 
 * <p>A default fileNamer is created and set as the active file namer on instantiation of the
 * FileNamerManager object, when another filenamer is loaded it will become the active fileNamer.</p>
 * 
 * <p>The default fileNamer can be made the active fileNamer again by simply calling:<br/>
 * <code>setActiveFileName(DefaultFileNamer.DEFAULT_FILENAMER_NAME);</code></p>
 * 
 * @see AbstractFileNamer
 * @author Andrew Keeling
 * @author Justin Waddell
 * @author Chris Bitmead
 */
public class FileNamerManager implements LoadManager {
    /**
     * String className -> FileNamer
     */
    protected Map<String, AbstractFileNamer> namers = new HashMap<String, AbstractFileNamer>();

    /**
     * Default file extension for Xena files.
     */
    public static final String DEFAULT_EXTENSION = "xena";
    
    /**
     * Default file filter to use when looking for Xena files.
     */
    public static final FileFilter DEFAULT_FILE_FILTER = new FileFilter() {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName().toLowerCase();
            if (name.endsWith("." + FileNamerManager.DEFAULT_EXTENSION)) {
                return true;
            }
            return false;
        }
        
    };

    private PluginManager pluginManager;

    private AbstractFileNamer activeFileNamer;

    private File destinationDir;
    
    /*
     * Indicates that a file namer has not been automatically loaded from a plugin,
     * or set using setActiveFileNamer. This removes a potential problem where
     * the user manually selects the DefaultFileNamer, but then loads a new plugin
     * with a FileNamer which would overwrite the DefaultFileNamer.
     */ 
    private boolean activeFileNamerUnchanged = true;

    /**
     * Default constructor. By default, this is called from the plugin manager.
     * @param pluginManager
     */
    public FileNamerManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        AbstractFileNamer defaultNamer = new DefaultFileNamer();
        defaultNamer.setFileNamerManager(this);
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

    // Implemented as part of LoadManager interface.
    public boolean load(JarPreferences preferences) throws XenaException {
        try {
            PluginLoader loader = new PluginLoader(preferences);
            List transes = loader.loadInstances("fileNamers");
            Iterator it = transes.iterator();

            while (it.hasNext()) {
                AbstractFileNamer namer = (AbstractFileNamer) it.next();
                namer.setFileNamerManager(this);
                namers.put(namer.getClass().getName(), namer);

                if (activeFileNamerUnchanged) {
                    activeFileNamer = namer;
                    activeFileNamerUnchanged = false;
                }
            }
            return !transes.isEmpty();
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
     * 
     * @return Collection of File Namers
     */
    public Collection<AbstractFileNamer> getFileNamers() {
        return namers.values();
    }

    /**
     * Get a list of the name of the loaded file namers
     * 
     * @return Collection of File Namer names
     */
    public Collection<String> getListOfNamers() {
        return namers.keySet();
    }

    /**
     * Find a FileNamer object given its class name.
     * 
     * @param name
     *            class name
     * @return FileNamer
     */
    public AbstractFileNamer lookupByClassName(String name) {
        return namers.get(name);
    }

    /**
     * return the Active file namer.
     * 
     * @return FileNamer
     */
    public AbstractFileNamer getActiveFileNamer() {
        return activeFileNamer;
    }

    /**
     * Set active filenamer to be specified filenamer. return true on success.
     * 
     * @param fileNamer
     * @return boolean
     */
    public boolean setActiveFileNamer(AbstractFileNamer fileNamer) {
        if (fileNamer != null) {
            activeFileNamer = fileNamer;
            activeFileNamerUnchanged = false;
            return true;
        }
        return false;
    }

    /**
     * Set active filenamer by name. return true on success.
     * 
     * @param fileNamer
     * @return boolean
     */
    public boolean setActiveFileNamer(String name) {
        if (namers.get(name) != null) {
            activeFileNamer = namers.get(name);
            activeFileNamerUnchanged = false;
            return true;
        }
        return false;
    }

    public void complete() {
    }

    /**
     * Return the destination directory.
     * If destinationDir has been set just return destinationDir. If
     *  it has not been initialised, get the current working directory
     *  from the system properties, return that. If there is a problem
     *  with this for some reason, return null.
     * 
     * @return Returns the destinationDirectory. It may return null
     * if the destinationDir is not initialised and the user.dir system propery
     * is not set or incorrect for some reason.
     */
    public File getDestinationDir() {
        if (destinationDir != null) {
            return destinationDir;
        }
        // return new File(System.getProperty("user.dir"));
        String currentDirectoryString = System.getProperty("user.dir");
        if (currentDirectoryString != null) {
            File currentDirectory = new File(currentDirectoryString);
            if (currentDirectory.exists() && currentDirectory.isDirectory()) {
                destinationDir = currentDirectory;
                return currentDirectory;
            }
        }
        return null;
    }

    /**
     * @param destinationDirectory
     *            The new value to set destinationDirectory to.
     */
    public void setDestinationDir(File destinationDirectory) {
        if (!destinationDirectory.exists()
                || !destinationDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Destination directory must be a valid directory!");
        }
        this.destinationDir = destinationDirectory;
    }

}
