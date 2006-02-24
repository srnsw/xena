package au.gov.naa.digipres.xena.kernel;

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

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.TypePrinterManager;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;

/**
 * Manages the loading of plugins.
 * 
 * @author Chris Bitmead.
 * @created April 24, 2002
 */
public class PluginManager {
    
    
    protected static PluginManager theSingleton;

    static JarPreferences prefs = (JarPreferences) JarPreferences.userNodeForPackage(PluginManager.class);

    /**
     * These are all the managers for the various plugin functions.
     */
    private TypeManager typeManager = TypeManager.singleton();
    private GuesserManager guesserManager = GuesserManager.singleton();
    private NormaliserManager normaliserManager = NormaliserManager.singleton();
    private MetaDataWrapperManager filterManager = MetaDataWrapperManager.singleton();
    private DecoderManager decoderManager = DecoderManager.singleton();
    private FileNamerManager fileNamerManager = FileNamerManager.singleton();
    private TypePrinterManager typePrinterManager = TypePrinterManager.singleton();
    private BatchFilterManager batchFilterManager = BatchFilterManager.singleton();
    private ViewManager viewManager = ViewManager.singleton();
    
    // Going against the trend and not making PropertiesManager a singleton...
    // Will probably change the rest of the Managers at some stage in the future
    private PropertiesManager propertiesManager = new PropertiesManager();
    

    /**
     * Load managers list. This is used as a shortcut to allow us to iterate
     * through the managers without going through each one. very useful!!!
     */
    protected List<Object> loadManagers = new ArrayList<Object>();
    // add all our manages to the loadManagers list.
    {
        loadManagers.add(typeManager);
        loadManagers.add(guesserManager);
        loadManagers.add(normaliserManager);
        loadManagers.add(filterManager);
        loadManagers.add(decoderManager);
        loadManagers.add(fileNamerManager);
        loadManagers.add(typePrinterManager);
        loadManagers.add(batchFilterManager);
        loadManagers.add(viewManager);
        loadManagers.add(propertiesManager);
    }

    /**
     * The deserialised class loader
     */
    DeSerializeClassLoader deserClassLoader = new DeSerializeClassLoader(getClass().getClassLoader());

    /**
     * A list of all the names of the plugins that have been loaded already.
     */
    private ArrayList<String> loadedPlugins = new ArrayList<String>();
    
    /**
     * A list of all the names of plugins that could not be loaded.
     */
    private ArrayList<String> unloadablePlugins = new ArrayList<String>();

    
    public static PluginManager singleton() {
        synchronized (PluginManager.class) {
            if (theSingleton == null) {
                theSingleton = new PluginManager();
            }
        }
        return theSingleton;
    }

    public PluginManager() {
        // Each of the different types of classes is loaded and managed by a
        // Manager class. Here we enumerate all the Managers.
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
     * The plug in manager really shouldnt have to go out and find the
     * fuckers...
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
                        //notout
                        //System.out.println("Plugin [" + newEntry.getName()+ "] already loaded!");
                    } else {
                        dependancyGraph.put(newEntry.getName(), newEntry);
                    }
                }
            }
        }
        // Add plugins from the plugins directory
        checkDirPlugins(dependancyGraph);
        
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
            //sysout - write out plugins that were not able to be loaded for some reason.
            System.out.println("Unloadable plugins list:");
            Iterator unloadableIterator = unloadablePlugins.iterator();
            while (unloadableIterator.hasNext()) {
                String pluginName = (String)unloadableIterator.next();
            }
        }
        
        // Do any finalization work in each LoadManager
        Iterator loadManagerIterator = loadManagers.iterator();
        while (loadManagerIterator.hasNext()) {
            LoadManager loadManager = (LoadManager) loadManagerIterator.next();
            //notout
            //System.out.println("finalising load manager:" + loadManager.getClass().toString());
            loadManager.complete();
        }
    }

    
    public void loadPlugin(String pluginName) throws IOException, XenaException {
        Collection<String> pluginList = new Vector<String>();
        pluginList.add(pluginName);
        loadPlugins(pluginList);
    }

    
    public void loadPlugins(File pluginLocation) throws IOException, XenaException {
        ArrayList<File> pluginFiles = new ArrayList<File>();
        ArrayList<String> pluginNames = new ArrayList<String>();
        
        
        if (pluginLocation.isDirectory()) {
            File list[]= pluginLocation.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
            for (int i = 0; i < list.length; i++){
                pluginFiles.add(list[i]);
            }
        } else if (pluginLocation.isFile()) {
            // Ugly hack to accept jar files instead of just directories.
            // Check to make sure it is in fact a jar then off we go.
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
        
        for (Iterator iter = pluginFiles.iterator(); iter.hasNext();){
            File pluginFile = (File)iter.next();
            String name = getPluginName(pluginFile);        
            try {
                deserClassLoader.addURL(pluginFile.toURL());
                pluginNames.add(name);
                
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        loadPlugins(pluginNames);
    }

    /**
     * Searches a list of directories to load plugins from. First we search all
     * the directories listed in Xena property "pluginDir". This property is
     * most useful for debugging. Then we search in the getHomePluginDir(). This
     * is the most likely scenario for production use.
     * 
     * @param dependancyGraph
     *            Description of Parameter
     */
    private void checkDirPlugins(Map<String, PluginEntry> dependancyGraph) {
        StringTokenizer st = new StringTokenizer(prefs.get("pluginDir", ""));
        while (st.hasMoreTokens()) {
            String dirName = st.nextToken();
            checkPluginsDirectory(dependancyGraph, new File(dirName));
        }
        File homePluginDir = PluginLocator.getHomePluginDir();
        if (homePluginDir != null) {
            checkPluginsDirectory(dependancyGraph, homePluginDir);
        }
    }

    /**
     * Load all the plugins in a particular directory. We only accept files
     * ending in .jar. Were we to accept anything, then we would load JBuilder
     * backup files which is a pain and confusion in development.
     * 
     * @param dependancyGraph
     *            Description of Parameter
     * @param directory
     *            Description of Parameter
     */
    private void checkPluginsDirectory(Map<String, PluginEntry> dependancyGraph, File directory) {
        File[] pluginFiles = null;

        if (directory.isDirectory()) {
            pluginFiles = directory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
        } else if (directory.isFile()) {
            // Ugly hack to accept jar files instead of just directories.
            String theFilename = directory.getName();
            String extension = "";
            int whereDot = theFilename.lastIndexOf('.');
            if (0 < whereDot && whereDot <= theFilename.length() - 2) {
                extension = theFilename.substring(whereDot + 1);
            }
            if (extension.equals("jar")) {
                File[] list = { directory };
                pluginFiles = list;
            }
        }

        if (pluginFiles != null) {
            for (int i = 0; i < pluginFiles.length; i++) {
                File pluginFile = null;
                try {
                    pluginFile = pluginFiles[i];
                    if (pluginFile.isFile()) {
                        //notout
                        //System.out.println("Found a file in the plugin folder:" + pluginFile.getName());
                        deserClassLoader.addURL(pluginFile.toURL());
                        String name = getPluginName(pluginFile);
                        //notout
                        //System.out.println("The name of the plugin: " + name);

                        
                        //so in *theory* the classes for our plugin should all be loaded.
                        // then again... in *theory* communism works.
                        PluginEntry pluginEntry = new PluginEntry(this, name);
                        dependancyGraph.put(name, pluginEntry);
                        
                    }
                } catch (Exception e) {
                    // Don't die because of one bogus plugin
                    System.err.println("Error loading Plugin file: "
                            + pluginFile + ": " + e);
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    /**
     * Initialise a single plugin given its name and an appropriate class
     * loader.
     * 
     * @param name
     *            name of plugin
     * @param cl
     *            ClassLoader to use for loading the plugin
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
        //notout
        //System.out.println("Loading plugin: " + name);
        //System.out.flush();
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
            //notout
            //System.out.println("Nothing loaded for plugin: " + name);
            unloadablePlugins.add(name);
        }
    }

//    
//    /**
//     * Add a plugin to the dependancy graph plan.
//     * 
//     * @param name name of plugin
//     */
//    protected void addPluginToPlan(Map<String, PluginEntry> dependancyGraph, String name)
//            throws IOException, XenaException {
//
//        
//        
//        
//        
//        JarPreferences root = (JarPreferences) JarPreferences.userRoot();
//        try {
//            // Check if the preferences file exists
//            System.out.println("looking for: " + name);
//            if (!root.jarNodeExists(name, deserClassLoader)) {
//                throw new XenaException("Plugin: " + name
//                        + " does not contain properties");
//            }
//        } catch (BackingStoreException ex) {
//            throw new XenaException(ex);
//        }
//        JarPreferences pp = (JarPreferences) root.node(name, deserClassLoader);
//        if (dependancyGraph.containsKey(name)) {
//            // If we get here, then you are somehow trying to load a plugin
//            // twice. This is potentially bad, but we don't bomb because of it.
//            System.err.println("Cannot load plugin: " + name + " more than once");
//        } else {
//            // Each plugin may contain in the preferences.properties a
//            // "dependancies" property, that lists the plugins it depends on.
//            // The order plugins are loaded in can affect behaviour of certain
//            // parts of Xena like what the Guesser is more prone to choosing.
//            
//            PluginEntry pluginEntry = new PluginEntry(null, null, null);
//            
//            dependancyGraph.put(name, pluginEntry);
//        }
//    }

    
    
    /**
     * Resolve all the dependencies in our dependancy graph. At the end
     * we will have the pluginPlan which is an ordered list that tells 
     * us the order to load our plugins.
     * 
     * 
     * 
     * The rules for adding plugins to the dependancy graph are as follows: ->
     * If the dependancy graph still contains the plugin that we depend on, wait ->
     * If the number of remaining plugins is equal to the number of plugins with
     * dependancies marked 'all', then we add any plugins marked 'all' *-> If
     * the dependancy that we require is nowhere, then throw an exception. This
     * is actually a non-trivial problem.
     * 
     * At the end of this function, we have the following situation...
     * 
     * 
     * 
     */
    protected ArrayList resolveDependancies(Map dependancyGraph) throws IOException, XenaException {
        ArrayList<String> pluginPlan = new ArrayList<String>();

        //notout
        //System.out.println("dependancy graph entries:");
        //for (Iterator iter = dependancyGraph.values().iterator(); iter.hasNext();) {
            //notout
            //System.out.println(iter.next().toString());
        //}
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
                
                // if we are missing some dependancies completely, we are in bug trouble!\
                // put our plugin into the unloadable plugins list.
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
    
    
    /*****************************
     * 
     * GETTERS AND SETTERS FOLLOW
     * 
     ******************************/
    

    public String toString() {
        return "this is a plugin manager. RAAARRRGGHHH!";
    }


    public ArrayList getLoadedPlugins() {
        return loadedPlugins;
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
     * @return Returns the filterManager.
     */
    public MetaDataWrapperManager getFilterManager() {
        return filterManager;
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
