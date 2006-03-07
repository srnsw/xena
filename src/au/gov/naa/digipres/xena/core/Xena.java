/*
 * Created on 9/09/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.kernel.BatchFilterManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class Xena {
    /**
     * @author aak This class will represent an instance of XENA. it will be
     *         able to load plugins, normalise, and a few other odds and ends.
     * 
     * It should act as an intermediatery between everything and xena. Xena
     * should be a 'black box', and called from any application that needs
     * preservation services. This could be a stand alone preservation tool,
     * or part of something that is a workflow type thingimy.
     * 
     */
    
    
    private PluginManager pluginManager = PluginManager.singleton();
    
    /**
     * Constructor.
     * Empty! There really isn't much to do here!
     */
    public Xena(){
        // empty constructor... (?)
    }
    
    /**
     * Return the current version...
     * TODO Find a better way of doing this.
     * @return String
     */
    public static String getVersion(){
        return "Version 2.0.5a";
    }
    
    
    /**
     * Load a single plugin by name.
     */
    public void loadPlugin(String pluginName){
        try{
            pluginManager.loadPlugin(pluginName);
        } catch (IOException e){
            System.err.println("Unable to load plugin: " + pluginName);
            e.printStackTrace();
        } catch (XenaException e){
            System.err.println("Unable to load plugin: " + pluginName);
            e.printStackTrace();
        }
    }
    
    
    /**
     * Load a number of plugins by name. The plugins should already be on the
     * class path.
     * 
     * @param pluginList
     *            the String names of the plugins to be loaded
     */
    public void loadPlugins(List<String> pluginList) {
        try{
            pluginManager.loadPlugins(pluginList);
        } catch (IOException e){
            System.err.println("Unable to load plugin.");
            e.printStackTrace();
        } catch (XenaException e){
            System.err.println("Unable to load plugin.");
            e.printStackTrace();
        }
    }
    
    
    /**
     * Load plugins from a file object. The file object is either jar file or a
     * folder containing one or more jar files
     * 
     * @param pluginLocation
     */
    public void loadPlugins(File pluginLocation) throws XenaException, IOException {
            pluginManager.loadPlugins(pluginLocation);
    }
    
    
    /**
     * To string method. Currently doesnt do much.
     * 
     * @return The name of this object :)
     */
    public String toString(){
        // ultimately, this should probably list all our guessers, normalisers etc.
        return "Xena";
    }
    
    
    /**
     * Return the plugin Manager
     * 
     * @return Returns the pluginManager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    
    /* 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * 
     * GUESSER STUFF
     * 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
    */
    
    /**
     * Return the guesses for a given XenaInputSource sorted by likelihood that they are in fact the
     * most likely guess.
     * 
     * @param xis
     * @return A list of Guess objects for the XenaInputSource.
     * @throws XenaException
     * @throws IOException
     */
    public List<Guess> getGuesses(XenaInputSource xis) throws XenaException, IOException {
        return getPluginManager().getGuesserManager().getGuesses(xis);
    }
    
    /**
     * Return the best guess for this object. The guess is simply the xena type
     * and an Integer value corresponding to the value of a guess. The higher
     * the better. For guesses with equal 'value', the plugin loaded latest is
     * prefered.
     * 
     * @param xis
     * @return The best Guess for this XenaInputSource
     * @throws IOException
     */
    public Guess getBestGuess(XenaInputSource xis) throws IOException 
    {
    	return getPluginManager().getGuesserManager().getBestGuess(xis);
    }

    /**
     * Return the best guess for this object, with the given list of type
     * names disabled. The guess is simply the xena type
     * and an Integer value corresponding to the value of a guess. The higher
     * the better. For guesses with equal 'value', the plugin loaded latest is
     * prefered.
     * 
     * @param xis
     * @return The best Guess for this XenaInputSource
     * @throws IOException
     */
    public Guess getBestGuess(XenaInputSource xis, List<String> disabledTypeList) throws IOException 
    {
    	return getPluginManager().getGuesserManager().getBestGuess(xis, disabledTypeList);
    }

    /**
     * Return the best guess for this object. The guess is simply the xena type
     * and an Integer value corresponding to the value of a guess. The higher
     * the better. For guesses with equal 'value', the plugin loaded latest is
     * prefered.
     * 
     * @param xis
     * @return The best Guess for this XenaInputSource
     * @throws XenaException
     * @throws IOException
     */
    public Type getMostLikelyType(XenaInputSource xis) throws XenaException, IOException {
        List<Type> guessedTypes = getPluginManager().getGuesserManager().getPossibleTypes(xis);
        if (guessedTypes.size() != 0) {
            return guessedTypes.get(0);
        }
        throw new XenaException("No type returned for the input source!");
    }


    /* 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * 
     * LOG FUNCTIONS
     * 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
    */
   

    /* 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * 
     * FILENAMER FUNCTIONS
     * 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
    */

    /**
     * This returns the list of FileNamer objects that xena knows about.
     * 
     * @return The collection of FileNamers
     */
    public Collection<FileNamer> getFileNamers() {
        return pluginManager.getFileNamerManager().getFileNamers();
    }

    /**
     * This sets the currently active FileNamer to the fileNamer that has the name
     * specified.
     * 
     * @param fileNamerName the name of the fileNamer to be the active filenamer.
     * @return 
     * @throws XenaException in the case that the fileNamer named cannot be set to be the active filenamer.
     */
    public void setActiveFileNamer(String fileNamerName) throws XenaException {
        if (fileNamerName == null) {
            throw new XenaException("Unable to set active FileNamer to null.");
        }
        if (pluginManager.getFileNamerManager().setActiveFileNamer(
                fileNamerName) == false) {
            throw new XenaException(
                    "Unable to set active FileNamer. Specified FileNamer '"
                            + fileNamerName
                            + "' was not found or was unable to be loaded.");
        }
    }

    /**
     * This sets the currently active FileNamer to the fileNamer specified.
     * 
     * @param fileNamer the fileNamer to be the active filenamer.
     * @return 
     * @throws XenaException in the case that the specified fileNamer cannot be set to be the active filenamer.
     */
    public void setActiveFileNamer(FileNamer fileNamer) throws XenaException {
        if (fileNamer == null) {
            throw new XenaException("Unable to set active FileNamer to null.");
        }
        if (pluginManager.getFileNamerManager().setActiveFileNamer(fileNamer) == false) {
            throw new XenaException(
                    "Unable to set active FileNamer. Specified FileNamer '"
                            + fileNamer.toString()
                            + "' was not found or was unable to be loaded.");
        }
    }

    /**
     * This returns the currently active FileNamer.
     * 
     * @return The currently active FileNamer.
     */
    public FileNamer getActiveFileNamer() {
        return pluginManager.getFileNamerManager().getActiveFileNamer();
    }

    
    /* 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * 
     * WRAPPER FUNCTIONS
     * 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
    */

    /**
     * This returns a list of Meta Data Wrapper Plugins currently available. Each filter
     * consists of a name, an outer tag name, and a wrapper and unwrapper class.
     * 
     * @return The list of filters
     */
    public List<MetaDataWrapperPlugin> getMetaDataWrappers() {
        return pluginManager.getMetaDataWrapperManager().getMetaDataWrapperPlugins();
    }

    /**
     * This returns currently active meta data wrapper plugin.
     * 
     * @return The currently active wrapper.
     */
    public MetaDataWrapperPlugin getActiveMetaDataWrapperPlugin()  {
        return pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin();
    }

    /**
     * This returns currently active meta data wrapper plugin.
     * 
     * @return The currently active wrapper.
     */
    public XMLFilter getActiveWrapper()  throws XenaException {
        return pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
    }
    
    public void setActiveMetaDataWrapperPlugin(MetaDataWrapperPlugin metaDataWrapperPlugin) {
        pluginManager.getMetaDataWrapperManager().setActiveWrapperPlugin(metaDataWrapperPlugin);
    }
    
    /** 
     * Set the base path by which files should have their relative path recorded.
     * This is used by the filter manager to determine how to name the files
     * that come into xena. By default, this will usually be set to null - files will 
     * have their full names recorded in the meta data.
     * 
     * Specifically, all URIs for files will be set to be relative to the supplied base path.
     * This is useful as often the location of the file only important relative to something else.
     * For example, if the contents of a web server are to be normalised, it is useful to know 
     * where a file is relative to the base of the web server content, but not to, say, the C drive.
     * 
     * @param String the name of the base path
     * @return void
     * @throws XenaException - when the path is incorrect.
     */
    

    /**
     */
    public synchronized void setBasePath(String basePath) throws XenaException {
        File f = new File(basePath);
        if (!f.exists() || !f.isDirectory()) {
            throw new XenaException("Bad base path: " + basePath);
        }
        pluginManager.getMetaDataWrapperManager().setBasePathName(basePath);
    }
    
    /* 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * 
     * NORMALISE FUNCTIONS
     * 
     * -------------------------------------------------------------
     * -------------------------------------------------------------
     * -------------------------------------------------------------
    */
    
    
    /**
     * This returns the normaliser for a given Xena type.
     * 
     * @param type
     * @return The normaliser for this Xena Type.
     * @throws XenaException
     */
    public AbstractNormaliser getNormaliser(Type type) throws XenaException {
        return pluginManager.getNormaliserManager().lookup(type);
    }
    
    /**
     * Return the normaliser based on name - for example, "binary" returns the
     * binary normaliser.
     * 
     * @param name
     * @return a normaliser with the name specified.
     * @throws XenaException
     */
    public AbstractNormaliser getNormaliser(String name) throws XenaException {
        return pluginManager.getNormaliserManager().lookup(name);
    }
    
    
    /* -------------------------------------------------------------
     * NORMALISATION: NO NORMALISER SPECIFIED
     * -------------------------------------------------------------*/
    
    /**
     * Normalise the xena input source by getting the current working directory, active fileNamer 
     * and active wrapper, and then call:
     * normalise(XenaInputSource, File, FileNamer, XMLFilter)
     * Return the only NormaliserDataStore that is returned as a result of the normalisation.
     * 
     * @param xis - the xena input source to be normalised
     * @return A NormaliserDataStore object with the results of the normalisation.
     * @throws XenaException
     */
    public NormaliserResults normalise(XenaInputSource xis) throws XenaException{
        
        File destinationDir = getCurrentWorkingDir();
        FileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
        XMLFilter wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
        NormaliserResults results = normalise(xis, destinationDir, fileNamer, wrapper);
        if (results != null) {
            return results;
        }
        throw new XenaException("No results returned!");
    }
    
    
    /**
     * This will normalise the xena input source to the destination directory, by 
     *  getting the active fileNamer and wrapper, and then calling:
     * normalise(XenaInputSource, File, FileNamer, XMLFilter) 
     * 
     * Return the NormaliserDataStore that is generated as a result of the
     * normalisation
     * 
     * @param xis - the XenaInputSource to normalise
     * @param destinationDir - destination directory for the normalised files
     * @return A NormaliserDataStore object with the results of the normalisation.
     * @throws XenaException
     */
    public NormaliserResults  normalise(XenaInputSource xis, File destinationDir) throws XenaException {
        FileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
        XMLFilter wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
        NormaliserResults results = normalise(xis, destinationDir, fileNamer, wrapper);
        if (results != null) {
            return results;
        }
        throw new XenaException("No results returned!");
    }
    
    
    /**
     * This will normalise the xena input source to the destination dir using the fileNamer
     * and wrapper. As no normaliser has been specfied, guess the type of the xis, 
     * and get the appropriate normaliser. Then use the specified fileNamer, 
     * wrapper and destination dir to normalise the files.
     * 
     * Return a list of NormaliserDataStore objects for each xena input source.
     * 
     * @param xis - the XenaInputSource to normalise
     * @param destinationDir - destination dierectory for the normalised files
     * @param fileNamer - an instance of a FileNamer object to return the output file
     * @param wrapper - an instance of an XMLFilter to 'wrap' the normalised data stream in meta data.
     * @return A NormaliserDataStore object with the results of the normalisation.
     * @throws XenaException
     */
    public NormaliserResults normalise(XenaInputSource xis, File destinationDir, FileNamer fileNamer, XMLFilter wrapper) throws XenaException {
        NormaliserResults results = new NormaliserResults(xis);
        
        if (xis.getType() == null)
        {
	        // find the most likely type for this XIS...
	        Guess bestGuess = null;
	        try {
	            bestGuess = getBestGuess(xis);
	        } catch (IOException e){
	            e.printStackTrace(System.out);
	        }
	        
	        if (bestGuess == null) {
	            throw new XenaException("No valid guess returned for this input.");
	        }
	        xis.setType(bestGuess.getType());
        }
        
        AbstractNormaliser normaliser = pluginManager.getNormaliserManager().lookup(xis.getType());
        
        if (normaliser == null) {
            throw new XenaException("No normaliser for this input.");
        }
        try {

            results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
            
        } catch (IOException e) {
            throw new XenaException(e);
        }            
        
        return results;
    }
    
    
    
    

    /* -------------------------------------------------------------
     * NORMALISATION: NORMALISER SPECIFIED
     * -------------------------------------------------------------*/
    

    
    /**
     * Normalise a list of XenaInputSources using a specified normaliser. For
     * example, the binary normaliser :) Returns a list of NormaliserDataStore
     * objects corresponding to each XenaInputSource.
     * 
     * @param   xis - the XenaInputSource to normalise
     * @param   normaliser - a instance of a normaliser to use.
     * @return  A NormaliserDataStore object with the results of the normalisation.
     * @throws  XenaException
     */
    public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser) throws XenaException {
        NormaliserResults results = new NormaliserResults(xis);
        FileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
        XMLFilter wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
        File destinationDir = getCurrentWorkingDir();
        try {
            results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
        } catch (IOException e) {
            throw new XenaException(e);
        }
        
        return results;
    }

    
    /**
     * Normalise a XenaInputSource using a specified normaliser. For
     * example, the binary normaliser :) 
     * 
     * Returns a NormaliserDataStore object with the results of the normalisation
     * for a particular XenaInputSource. Send the output files to the specified destination.
     * 
     * @param xis - the XenaInputSource to normalise
     * @param normaliser - a instance of a normaliser to use.
     * @param destinationDir - destination dierectory for the normalised files
     * @return A NormaliserDataStore object with the results of the normalisation.
     * @throws XenaException
     */
    public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
        NormaliserResults results = new NormaliserResults(xis);
        FileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
        XMLFilter wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
        
        System.out.println(wrapper.getClass().getName());
        
        try {
            results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
        } catch (IOException e) {
            throw new XenaException(e);         
        }
        return results; 
    }
    
    /**
     * Normalise the list of XenaInputSources using the specified normaliser,
     * FileNamer, wrapper and send the results to the specified destination
     * directory. Return the list of NormaliserDataStore objects corresponding
     * to each XenaInputSource.
     * 
     * @param xis - the XenaInputSource to normalise
     * @param normaliser - a instance of a normaliser to use.
     * @param destinationDir - destination dierectory for the normalised files
     * @param fileNamer - an instance of a FileNamer object to return the output file
     * @param wrapper - an instance of an XMLFilter to 'wrap' the normalised data stream in meta data.
     * @return A NormaliserDataStore object with the results of the normalisation.
     * @throws XenaException - 
     */
    public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir, FileNamer fileNamer, XMLFilter wrapper) throws XenaException {
        NormaliserResults results = new NormaliserResults(xis);
        try {
            results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
        } catch (IOException e) {
            throw new XenaException(e);
        }
        return results; 
    }
    
    /**
     * Get the current working directory from the system properties.
     * 
     * @return a File object for the current working directory.
     * @throws XenaException
     */
    private File getCurrentWorkingDir() throws XenaException {
        String currentDirectoryString = System.getProperty("user.dir");
        if (currentDirectoryString == null) {
            throw new XenaException("Cant get current working directory!");
        }
        File currentDirectory = new File(currentDirectoryString);
        if (!currentDirectory.exists() && !currentDirectory.isDirectory()) {
            throw new XenaException("Problem with current working directory!");
        }
        return currentDirectory;
    }

    /*
     *-------------------------------------------
     *-------------------------------------------
     *-------------------------------------------
     * Export Functions
     *-------------------------------------------
     *-------------------------------------------
     *-------------------------------------------
     */
    
    public ExportResult export(XenaInputSource xis, File destinationDir) throws XenaException {
        try {
            return pluginManager.getNormaliserManager().export(xis, destinationDir);
        } catch (IOException iox) {
            throw new XenaException(iox);
        } catch (ParserConfigurationException pce) {
            throw new XenaException(pce);
        } catch (SAXException se) {
            throw new XenaException(se);
        } 
    }
    
    public ExportResult export(XenaInputSource xis, File destinationDir, boolean overwrite) throws XenaException {
        try {
            return pluginManager.getNormaliserManager().export(xis, destinationDir, overwrite);
        } catch (IOException iox) {
            throw new XenaException(iox);
        } catch (ParserConfigurationException pce) {
            throw new XenaException(pce);
        } catch (SAXException se) {
            throw new XenaException(se);
        } 
    }
    
    
    
    
    /*
     *-------------------------------------------
     *-------------------------------------------
     *-------------------------------------------
     * Miscellaneous Functions
     *-------------------------------------------
     *-------------------------------------------
     *-------------------------------------------
     */
    
    /**
     * Returns a set of XISs which have been classified as 'children' by
     * the filters loaded in BatchFilterManager. Child XISs will be
     * normalised as part of the normalising process for their parent,
     * and thus should not be normalised separately.
     * 
     * @param xisColl
     * @return
     * @throws XenaException
     */
    public Map<XenaInputSource, NormaliserResults> 
    	getChildren(Collection<XenaInputSource> xisColl)
    	throws XenaException
    {
    	return BatchFilterManager.singleton().getChildren(xisColl);
    }
}
