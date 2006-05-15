package au.gov.naa.digipres.xena.kernel.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.DeSerializeClassLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilterManager;
import au.gov.naa.digipres.xena.kernel.decoder.DecoderManager;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.TypePrinterManager;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;

/**
 * This class is repsonsible for managing the loading of plugins in to Xena.
 * 
 * 
 * 
 * @author Andrew Keeling
 * @author Justin Waddell
 * @author Chris Bitmead
 * 
 * @created April 24, 2002
 */
public class PluginManager {
    
    
    /*
     * These are the component managers that for this plugin Manager.
     */
    private TypeManager typeManager;
    private GuesserManager guesserManager;
    private NormaliserManager normaliserManager;
    private MetaDataWrapperManager metaDataWrapperManager;
    private DecoderManager decoderManager;
    private FileNamerManager fileNamerManager;
    private TypePrinterManager typePrinterManager;
    private BatchFilterManager batchFilterManager;
    private ViewManager viewManager;
    private PropertiesManager propertiesManager;
    

    /**
     * Load managers list. This is used as a shortcut to allow us to iterate
     * through the managers without going through each one. This is done in the
     * constuctor, since to instantiate a load manager we need to give it a reference
     * to the plugin manager.
     */
    private List<Object> loadManagers = new ArrayList<Object>();


    /**
     * The deserialised class loader
     */
    private DeSerializeClassLoader deserClassLoader = new DeSerializeClassLoader(getClass().getClassLoader());

    /**
     * A list of all the names of the plugins that have been loaded already.
     */
    private List<String> loadedPlugins = new ArrayList<String>();
    
    /**
     * A list of all the names of plugins that could not be loaded.
     */
    private ArrayList<String> unloadablePlugins = new ArrayList<String>();
    
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Plugin manager main constructor.
     * Initialise all the component managers required for Xena to run.
     * 
     * Provide each of the component managers a reference to the pluginManager
     * so that they can talk to each other using the pluginManager as a conduit.
     * ie the GuesserManager can talk to the TypeManager by calling: pluginManager.getTypeManager()
     *
     * Also initialise the loadManagers list which is used when loading a plugin.
     */
    public PluginManager() {
        // Each of the different types of classes is loaded and managed by a
        // Manager class. Here we enumerate all the Managers.
        typeManager = new TypeManager(this);
        guesserManager = new GuesserManager(this);
        normaliserManager = new NormaliserManager(this);
        metaDataWrapperManager = new MetaDataWrapperManager(this);
        decoderManager = new DecoderManager(this);
        fileNamerManager = new FileNamerManager(this);
        typePrinterManager = new TypePrinterManager(this);
        batchFilterManager = new BatchFilterManager(this);
        viewManager = new ViewManager(this);
        propertiesManager = new PropertiesManager(this);
        
        // add all our manages to the loadManagers list.
        loadManagers.add(typeManager);
        loadManagers.add(guesserManager);
        loadManagers.add(normaliserManager);
        loadManagers.add(metaDataWrapperManager);
        loadManagers.add(decoderManager);
        loadManagers.add(fileNamerManager);
        loadManagers.add(typePrinterManager);
        loadManagers.add(batchFilterManager);
        loadManagers.add(viewManager);
        loadManagers.add(propertiesManager);
    }


    /**
     * Given a Xena plugin jar file, we extract the "name.properties" file from the
     * top level, which tells us the official name of this plugin which
     * corresponds to the package name with slash separators. A typical name
     * would be au/gov/naa/digipres/xena/plugin/plaintext
     * 
     * @param pluginFile
     *            The Xena Plugin Jar File
     * @return String The internal name of the plugin
     * @throws IOException
     * @throws MalformedURLException
     */
    public static String getPluginName(File pluginFile) throws IOException,
            MalformedURLException {
        URL url = pluginFile.toURL();
        // we don't use deserClassLoader here because it would
        // have conflicting name.properties
        URLClassLoader cl = new URLClassLoader(new URL[] { url }, null);
        InputStream is = cl.getResourceAsStream("name.properties");
        if (is == null) {
            throw new IOException("Cannot find name.properties in plugin " + pluginFile);
        }
        Properties namep = new Properties();
        namep.load(is);
        String name = namep.getProperty("name");
        return name;
    }



    /**
     * Loads the plugins. Figures out all the plugins that should be loaded,
     * figures out what order to load them in, and then loads them.
     * 
     * @param plugins A list of plugins to load.
     * @throws IOException
     * @throws XenaException
     * 
     * TODO: this should be overloaded -> there should be, say, loadPlugins(File
     * pluginsDir) loadPlugins(File[] pluginDirList) loadPlugins(Collection
     * plugins)
     * 
     * 
     */
    public void loadPlugins(Collection<String> plugins) throws IOException, XenaException {
        
        HashMap<String, PluginEntry> dependancyGraph = new HashMap<String, PluginEntry>();
        // Add plugins in collection to our dependency graph.
        if (plugins != null) {
            Iterator it = plugins.iterator();
            while (it.hasNext()) {
                String pluginName = (String) it.next();
                PluginEntry newEntry = null;
                try {
                    newEntry = new PluginEntry(this, pluginName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (newEntry != null) {
                    //addPluginToPlan(dependancyGraph, newEntry);
                    if (dependancyGraph.containsKey(newEntry.getName())) {
                    } else {
                        dependancyGraph.put(newEntry.getName(), newEntry);
                    }
                }
            }
        }
        
        // Figure out the dependancies and actually load the classes
        List sortedPluginPlan = resolveDependancies(dependancyGraph);

        // Now actually load the plugins that are ready to be loaded - the ones in the plugin plan!
        Iterator sortedPluginsIterator = sortedPluginPlan.iterator();
        while (sortedPluginsIterator.hasNext()) {
            String pluginName = (String) sortedPluginsIterator.next();
            initPlugin(pluginName, deserClassLoader);
        }
        
        /*
         * The following code snippet simply lists out each of the plugins that were not able to loaded
         * for some reason.
         */
        if (unloadablePlugins.size() != 0) {
            logger.finest("Unloadable plugins list: " + unloadablePlugins);
        }
        
        // Do any finalization work in each LoadManager
        Iterator loadManagerIterator = loadManagers.iterator();
        while (loadManagerIterator.hasNext()) {
            LoadManager loadManager = (LoadManager) loadManagerIterator.next();
            loadManager.complete();
        }
    }

    /**
     * This method allows Xena to load a plugin by name, this requires that the plugin class files
     * and required resources exist in the class path already.
     * 
     * This name should actually represent the location on the class path of the preferences.properties
     * file for this particular plugin.
     * 
     * For example, if Organisation X (which develops in the package space org.x) created a new
     * plugin with the preferences file located in the jar at "org.x.xena.plugin.new_plugin",
     * then when testing the application as follows:
     * #java -cp test_app.jar;xena.jar;new_plugin.jar org.x.xena.XenaTestApp
     * To load the plugin load by name:
     * xena.loadPlugin("org.x.xena.plugin.new_plugin");
     * 
     * @param pluginName - the name of the plugin (which should exist already in the class path)
     * @throws IOException
     * @throws XenaException
     */
    public void loadPlugin(String pluginName) throws IOException, XenaException {
        Collection<String> pluginList = new Vector<String>();
        pluginList.add(pluginName);
        loadPlugins(pluginList);
    }

    /**
     * This method allows xena to load a plugin by providing a file object for the location of the Xena
     * plugin or plugins. This file may be a single Jar file, or it may be a directory of Jar files.
     * <p>
     * If the plugin location is a directory, the contents will be listed and any Jars will be assumed to
     * be plugins. This is a common case when you wish to have a 'plugins' directory for an application
     * that uses Xena.</p>
     * <p>
     * If the plugin location is a file, then we check to ensure it is a valid jar file and then load it.
     * In terms of actually loading, what we really do is the following:
     * <ol>
     * <li> Add the jars to the class path, so we can load the classes therein,</li>
     * <li> Find the name.properties file, get the name of the plugin
     *      and add it to a list (aptly called pluginNames)</li>
     * <li> Load all the plugins be name (which is now possible since all the Jars have
     * been added to the classpath).</li>
     * </lo></p>
     * 
     * @param pluginLocation File that is either a specific plugin Jar File or a directory
     *      containing plugin jar files.
     * @throws IOException
     * @throws XenaException
     */
    public void loadPlugins(File pluginLocation) throws IOException, XenaException {
        // create a list of plugin files and plugin names...
        ArrayList<File> pluginFiles = new ArrayList<File>();
        ArrayList<String> pluginNames = new ArrayList<String>();
        
        if (pluginLocation.isDirectory()) {
            //handle directory -> add any jar files to our file list.
            File list[]= pluginLocation.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
            for (int i = 0; i < list.length; i++){
                pluginFiles.add(list[i]);
            }
        } else if (pluginLocation.isFile()) {
            //handle single file -> if its a jar file, add it to our file list.
            String theFilename = pluginLocation.getName();
            String extension = "";
            int whereDot = theFilename.lastIndexOf('.');
            if (0 < whereDot && whereDot <= theFilename.length() - 2) {
                extension = theFilename.substring(whereDot + 1);
            }
            if (extension.equals("jar")) {
                pluginFiles.add(pluginLocation);
            }   
        }
        
        // go through our plugin files list and try to add them to the class path,
        // and get the name from the name.properties file and add it to our plugin names list.        
        for (Iterator iter = pluginFiles.iterator(); iter.hasNext();){
            File pluginFile = (File)iter.next();
            String name = getPluginName(pluginFile);        
            try {
                deserClassLoader.addURL(pluginFile.toURL());
                pluginNames.add(name);
            } catch (MalformedURLException malformedURLException){
                // hmmm.... is this useful or necessary?
                // TODO - handle this better.
                malformedURLException.printStackTrace();
            }
        }
        // now all the files are on the class path, load our plugins by name!
        loadPlugins(pluginNames);
    }

    /**
     * Initialise a single plugin given its name and an appropriate class
     * loader.
     * 
     * @param name name of plugin
     * @param cl ClassLoader to use for loading the plugin
     */
    private void initPlugin(String name, ClassLoader cl) throws XenaException, IOException {
        JarPreferences root = (JarPreferences) JarPreferences.userRoot();
        JarPreferences pp = (JarPreferences) root.node(name, cl);
        initPlugin(name, pp);
    }

    /**
     * Initialise a single plugin given its name and its descriptive preferences
     * 
     * @param name name of plugin
     * @param preferences the preference values needed to load the plugin
     */
    private void initPlugin(String name, JarPreferences preferences)
            throws XenaException, IOException {
        boolean success = false;
        // Load every category of class
        Iterator it = loadManagers.iterator();
        while (it.hasNext()) {
            LoadManager lm = (LoadManager) it.next();
            if (lm.load(preferences)) {
                success = true;
            }
        }
        if (success) {
            loadedPlugins.add(name);
        } else {
            // Nothing loaded for a plugin usually means the preference file
            // and probably the classes too cannot be found. We cannot just say
            // "Plugin not found" because plugins can be loaded via the classpath
            // so that wouldn't be strictly true.
            unloadablePlugins.add(name);
        }
    }

    
    /**
     * Resolve all the dependencies in our dependancy graph. At the end
     * we will have the pluginPlan which is an ordered list that tells 
     * us the order to load our plugins.
     * 
     * The rules for adding plugins to the dependancy graph are as follows:<ul>
     * <li>If the dependancy graph still contains the plugin that we depend on, wait.</li>
     * 
     * <li>If the number of remaining plugins is equal to the number of plugins with
     * dependancies marked 'all', then we add any plugins marked 'all'</li>
     * 
     * <li>If the dependancy that we require is nowhere, then add it to the 
     * unloadable plugins list.</li>
     * </ul>
     * 
     * This is actually kind of tricky, and however the implementation is neatly
     * commented and not too difficult to understand now.
     * 
     * @param dependancyGraph
     * @return ArrayList<String> - Sorted list of plugins in the order that they are to be loaded. 
     */
    protected ArrayList<String> resolveDependancies(Map dependancyGraph) {
        ArrayList<String> pluginPlan = new ArrayList<String>();

        // start going through our whole dependancy graph
        while (dependancyGraph.size() != 0) {
            Map<String, PluginEntry> newDependancyGraph = new HashMap<String, PluginEntry>();
            // count of plugins that have dependency 'all'
            int numall = 0;
            // go through all the plugins in our dependancy graph...
            Iterator it = dependancyGraph.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) it.next();
                PluginEntry pluginEntry = (PluginEntry) mapEntry.getValue();

                // assume there are no outstanding, missing dependancies and we
                // are not looking for 'all'
                boolean hasOutstandingDependancies = false;
                boolean hasAllDependancies = false;
                boolean missingDependancies = false;

                // get the dependancy list
                List dependancies = pluginEntry.getDependancyList();
                Iterator dependancyIterator = dependancies.iterator();
                while (dependancyIterator.hasNext()) {
                    String dependancy = (String) dependancyIterator.next();
                    // if the dependancy is 'all', increment our all counter.
                    // this assumes that 'all' is the only entry in the
                    // dependancy list.
                    if (dependancy.equals("all")) {
                        numall++;
                        hasAllDependancies = true;
                    }
                    // if missing dependancies is true, we are in trouble.
                    // set our missingDependancies flag....
                    if (!dependancyGraph.containsKey(dependancy) 
                            && !pluginPlan.contains(dependancy) 
                            && !dependancy.equals("all")
                            && !loadedPlugins.contains(dependancy)) {
                        missingDependancies = true;
                    }
                    // if the dependancy that we are looking remains in the dependancy graph,
                    // simply increment our outstanding dependancy counter.
                    if (dependancyGraph.containsKey(dependancy)) {
                        hasOutstandingDependancies = true;
                    }
                }
                //so now we have finished processing the list of dependencies for this plugin.
                // time to act on the results!
                
                // if we are missing some dependancies completely, we are in big trouble!
                // in this case, put our plugin into the unloadable plugins list.
                if (missingDependancies) {
                    unloadablePlugins.add(pluginEntry.getName());
                }
                // if it has the 'all' dependancy tag...
                // in this case, check if the number of plugins with the all tag is
                // equal to the number of plugins left in the graph. if so, add it to
                // the plugin plan! If not add it to the new dependancy graph.
                else if (hasAllDependancies) {
                    if (numall == dependancyGraph.size()){
                        pluginPlan.add(pluginEntry.getName());
                    } else {
                        newDependancyGraph.put(pluginEntry.getName(), pluginEntry);
                    }
                }
                // finally, we check if the outstanding dependancies flag is set.
                // if so, simply put it into our 'new dependancy' graph, to be processed
                // in the next attempt!
                else if (hasOutstandingDependancies) {
                    newDependancyGraph.put(pluginEntry.getName(), pluginEntry);
                } 
                // Wow! we finally made it. if none of our flags are added, add it to
                // our pluginPlan!!!
                
                else {
                    pluginPlan.add(pluginEntry.getName());
                }
            }
            //set dependancy graph for remaining plugins, and then cycle through
            // these. by this stage, only the plugins with dependencies will be left,
            // and hopefully each run through will get some more. Huzzah!!!
            dependancyGraph = newDependancyGraph;
        }
        return pluginPlan;
    }
    
    
    //  *******************************
    //  *                             *
    //  * GETTERS AND SETTERS FOLLOW  *
    //  *                             *
    //  *******************************
    
    /**
     * Return the string representation of the plugin manager. At this stage the
     * plugin manager string representation is simply.... "Plugin manager!"
     * followed by a list of loaded plugins.
     * @return The string representation of the plugin manager.
     */
    public String toString() {
        StringBuffer returnBuffer = new StringBuffer("Plugin manager!");
        returnBuffer.append(System.getProperty("line.separator"));
        returnBuffer.append("Loaded plugins:");
        returnBuffer.append(System.getProperty("line.separator"));
        for (Iterator iter = getLoadedPlugins().iterator(); iter.hasNext();) {            
            returnBuffer.append(iter.next());
            returnBuffer.append(System.getProperty("line.separator"));
        }
        return new String(returnBuffer);
    }

    /**
     * Return the list of loaded plugins.
     * @return Returns the list of loaded plugins.
     */
    public List<String> getLoadedPlugins() {
        return loadedPlugins;
    }
    
    /**
     * @return Returns the unloadablePlugins.
     */
    public ArrayList<String> getUnloadablePlugins() {
        return unloadablePlugins;
    }

    /**
     * @return Returns the batchFilterManager.
     */
    public BatchFilterManager getBatchFilterManager() {
        return batchFilterManager;
    }

    /**
     * @return Returns the decoderManager.
     */
    public DecoderManager getDecoderManager() {
        return decoderManager;
    }

    /**
     * @return Returns the deserClassLoader.
     */
    public DeSerializeClassLoader getDeserClassLoader() {
        return deserClassLoader;
    }

    /**
     * @return Returns the fileNamerManager.
     */
    public FileNamerManager getFileNamerManager() {
        return fileNamerManager;
    }

    /**
     * @return Returns the metaDataManagerManager.
     */
    public MetaDataWrapperManager getMetaDataWrapperManager() {
        return metaDataWrapperManager;
    }

    /**
     * @return Returns the filterManager.
     */
    public MetaDataWrapperManager getFilterManager() {
        return metaDataWrapperManager;
    }

    /**
     * @return Returns the guesserManager.
     */
    public GuesserManager getGuesserManager() {
        return guesserManager;
    }

    /**
     * @return Returns the normaliserManager.
     */
    public NormaliserManager getNormaliserManager() {
        return normaliserManager;
    }

    /**
     * @return Returns the typeManager.
     */
    public TypeManager getTypeManager() {
        return typeManager;
    }

    /**
     * @return Returns the typePrinterManager.
     */
    public TypePrinterManager getTypePrinterManager() {
        return typePrinterManager;
    }
    
    /**
     * @return Returns the class loader
     */
    public DeSerializeClassLoader getClassLoader() {
        return deserClassLoader;
    }

    /**
     * @return Returns the viewManager.
     */
    public ViewManager getViewManager() {
        return viewManager;
    }

	/**
	 * @return Returns the propertiesManager.
	 */
	public PropertiesManager getPropertiesManager()
	{
		return propertiesManager;
	}
    
}
