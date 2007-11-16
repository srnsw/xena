/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 9/09/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/** 
 * This class represents an instance of XENA. it will be
 * able to load plugins, normalise, and a few other odds and ends.
 * 
 * It should act as an intermediary between everything and xena. Xena
 * should be a 'black box', and called from any application that needs
 * preservation services. This could be a stand alone preservation tool,
 * or part of something that is a workflow type thingimy.
 * 
 * Note that it allows access to the Xena objects by allowing
 * applications to get a reference to the plugin manager, which will
 * then allow users to get component plugin managers and so on.
 * 
 * @version 0.2
 */
public class Xena {

	/*
	 * This the Xena object's Plugin manager
	 */
	private PluginManager pluginManager = new PluginManager(this);

	// private PluginManager pluginManager = PluginManager.singleton();

	/**
	 * Class Constructor.
	 */
	public Xena() {
		// empty constructor... (?)
	}

	/**
	 * Return the current version.
	 * 
	 * @return String
	 */
	public static String getVersion() {
		return ReleaseInfo.getVersion();
	}

	/**
	 * Load a single plugin by name. The plugin should exist on
	 * the class path. If the plugin is unable to found, then
	 * a XenaException may be thrown.
	 * <p>
	 * This is often the preferred way of loading plugins,
	 * since if a third party application is asking Xena to
	 * load a number of plugins and for any reason can not load
	 * one, this will allow the calling application to know
	 * exactly which plugin has failed to load. However, since
	 * plugins have dependencies, it is often easier to simply
	 * use the method loadPlugins(List&#60String&#62 pluginList)</p>
	 * 
	 *
	 * @param pluginName The name of the plugin
	 * @throws XenaException
	 * @see #loadPlugins(List)
	 */
	public void loadPlugin(String pluginName) throws XenaException {
		List<String> pluginNameList = new ArrayList<String>();
		pluginManager.loadPlugins(pluginNameList);
	}

	/**
	 * Load a number of plugins by name. The plugins should already be on the
	 * class path.
	 * <p>
	 * This method should be used when a number of plugins are to be loaded through
	 * Xena, especially when some of these plugins have dependencies. Using this
	 * method, the plugin manager will actually load the plugins in the correct
	 * order to ensure that any dependencies are correctly handled.
	 * </p><p>
	 * Limitations: When loading plugins with this method there is a potential
	 * problem that if there is a major error loading a plugin, then it may be 
	 * difficult to work out which plugins were loaded.
	 * </p>
	 * @param pluginList The String names of the plugins to be loaded
	 * @throws XenaException If there is an exception while loading plugins.
	 */
	public void loadPlugins(List<String> pluginList) throws XenaException {
		pluginManager.loadPlugins(pluginList);
	}

	/**
	 * Load plugins from a file object. The file object is either jar file or a
	 * folder containing one or more jar files.
	 * 
	 * @param pluginLocation
	 * @throws XenaException In case of plugin being unable to be loaded for some reason
	 * @throws IOExcetpion In case of plugin being unable to be loaded for some reason
	 */
	public void loadPlugins(File pluginLocation) throws XenaException, IOException {
		pluginManager.loadPlugins(pluginLocation);
	}

	/**
	 * Return a string representation of this object. 
	 * Currently doesn't do much - simply returns the string Xena.
	 * 
	 * @return The name of this object
	 */
	@Override
	public String toString() {
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
	 * most likely guess. Guesses of equal likelihood are ranked non deterministically. Or alphabetically,
	 * which, for Xena, amounts to pretty much the same thing.
	 * <p>
	 * This method makes all the guessers perform a guess on the object, which results is computationally
	 * expensive. Unless all possible guesses are required, it is recommended that the method
	 * getBestGuess(XenaInputSource xis) be used instead.
	 * </p>
	 * 
	 * @param xis
	 * @return A list of Guess objects for the XenaInputSource.
	 * @throws XenaException
	 * @throws IOException
	 * @see #GuesserManager.getGuesses(XenaInputSource xis) 
	 */
	@SuppressWarnings("unused")
	public List<Guess> getGuesses(XenaInputSource xis) throws XenaException, IOException {
		// TODO: Remove the unused XenaException and IOException declarations. 
		// Removing them now would cause errors in calling methods which catch these exceptions
		return getPluginManager().getGuesserManager().getGuesses(xis);
	}

	/**
	 * Return the best guess for this object. The guess is simply the xena type
	 * and an Integer value corresponding to the value of a guess. The higher
	 * the better. For guesses with equal 'value', the plugin loaded latest is
	 * preferred.
	 * 
	 * @see #getBestGuess(XenaInputSource, List)
	 * 
	 * @param xis
	 * @return The best Guess for this XenaInputSource
	 * @throws IOException
	 */
	public Guess getBestGuess(XenaInputSource xis) throws IOException {
		return getPluginManager().getGuesserManager().getBestGuess(xis);
	}

	/**
	 * Return the best guess for this object, with the given list of type
	 * names disabled. The guess is simply the xena type
	 * and an Integer value corresponding to the value of a guess. The higher
	 * the better. For guesses with equal 'value', the plugin loaded latest is
	 * preferred.
	 * 
	 * @param xis
	 * @param disabledTypeList A list of strings that are the names of types that
	 *                          are disabled.
	 * @return The best Guess for this XenaInputSource
	 * @throws IOException 
	 */
	public Guess getBestGuess(XenaInputSource xis, List<String> disabledTypeList) throws IOException {
		return getPluginManager().getGuesserManager().getBestGuess(xis, disabledTypeList);
	}

	/**
	 * Return the most likely type for this object.
	 * 
	 * @param xis
	 * @return The best Guess for this XenaInputSource
	 * @throws XenaException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public Type getMostLikelyType(XenaInputSource xis) throws XenaException, IOException {
		// TODO: Remove the unused IOException declaration. 
		// Removing it now would cause errors in calling methods which catch this exception
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
	public Collection<AbstractFileNamer> getFileNamers() {
		return pluginManager.getFileNamerManager().getFileNamers();
	}

	/**
	 * This sets the currently active FileNamer to the fileNamer that has the name
	 * specified.
	 * 
	 * @param fileNamerName the name of the fileNamer to be the active filenamer.
	 * @throws XenaException in the case that the fileNamer named cannot be set to be the active filenamer.
	 */
	public void setActiveFileNamer(String fileNamerName) throws XenaException {
		if (fileNamerName == null) {
			throw new XenaException("Unable to set active FileNamer to null.");
		}
		if (pluginManager.getFileNamerManager().setActiveFileNamer(fileNamerName) == false) {
			throw new XenaException("Unable to set active FileNamer. Specified FileNamer '" + fileNamerName
			                        + "' was not found or was unable to be loaded.");
		}
	}

	/**
	 * This sets the currently active FileNamer to the fileNamer specified.
	 * 
	 * @param fileNamer the fileNamer to be the active filenamer.
	 * @throws XenaException in the case that the specified fileNamer cannot be set to be the active filenamer.
	 */
	public void setActiveFileNamer(AbstractFileNamer fileNamer) throws XenaException {
		if (fileNamer == null) {
			throw new XenaException("Unable to set active FileNamer to null.");
		}
		if (pluginManager.getFileNamerManager().setActiveFileNamer(fileNamer) == false) {
			throw new XenaException("Unable to set active FileNamer. Specified FileNamer '" + fileNamer.toString()
			                        + "' was not found or was unable to be loaded.");
		}
	}

	/**
	 * This returns the currently active FileNamer.
	 * 
	 * @return The currently active FileNamer.
	 */
	public AbstractFileNamer getActiveFileNamer() {
		return pluginManager.getFileNamerManager().getActiveFileNamer();
	}

	/**
	 * Set the active directory to output Xena files to. This may be overridden at any time by
	 * specifying the destination directory to xena, in which case the primary output file of the
	 * normalisation will be sent to that directory.
	 * 
	 * @param the output directory to set the 
	 */
	public void setDestinationDir(File destinationDir) {
		pluginManager.getFileNamerManager().setDestinationDir(destinationDir);
	}

	/**
	 * Return the destination directory that Xena is currently outputting to.
	 * 
	 * @return the current destination directory.
	 */
	public File getDestinationDir() {
		return pluginManager.getFileNamerManager().getDestinationDir();
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
	 * This returns a list of Meta Data Wrapper Plugins currently available. Each Meta
	 * Data Wrapper plugin consists of a name, an outer tag name, and a wrapper and 
	 * unwrapper class.
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
	public MetaDataWrapperPlugin getActiveMetaDataWrapperPlugin() {
		return pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin();
	}

	/**
	 * This returns currently active meta data wrapper plugin.
	 * 
	 * @return The currently active wrapper.
	 */
	public AbstractMetaDataWrapper getActiveWrapper() throws XenaException {
		return pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
	}

	/**
	 * Set the active meta data wrapper plugin to by name.
	 * 
	 * @param name - the name of the meta data wrapper plugin that should be the active one.
	 * @throws XenaException - in the case that the named plugin is not able to be loaded.
	 */
	public void setActiveMetaDataWrapperPlugin(String name) throws XenaException {
		MetaDataWrapperPlugin metaDataWrapperPlugin = pluginManager.getMetaDataWrapperManager().getMetaDataWrapperPluginByName(name);
		if (metaDataWrapperPlugin == null) {
			throw new XenaException("Unable to find meta data plugin named: " + name);
		}
		setActiveMetaDataWrapperPlugin(metaDataWrapperPlugin);
	}

	/**
	 * Set the active meta data wrapper plugin
	 * 
	 * @param metaDataWrapperPlugin - the MetaDataWrapperPlugin that should be active.
	 */
	public void setActiveMetaDataWrapperPlugin(MetaDataWrapperPlugin metaDataWrapperPlugin) {
		pluginManager.getMetaDataWrapperManager().setActiveWrapperPlugin(metaDataWrapperPlugin);
	}

	/** 
	 * Set the base path by which files should have their relative path recorded.
	 * This is used by the filter manager to determine how to name the files
	 * that come into xena. By default, this will usually be set to null - files will 
	 * have their full names recorded in the meta data.
	 * <p>
	 * Specifically, all URIs for files will be set to be relative to the supplied base path.
	 * This is useful as often the location of the file only important relative to something else.
	 * For example, if the contents of a web server are to be normalised, it is useful to know 
	 * where a file is relative to the base of the web server content, but not to, say, the C drive.
	 * </p>
	 * 
	 * @param String the name of the base path
	 * @return void
	 * @throws XenaException - when the path is incorrect.
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
	 * Returns the normaliser for a given Xena type.
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
	@SuppressWarnings("unused")
	public AbstractNormaliser getNormaliser(String name) throws XenaException {
		// TODO: Remove the unused XenaException declaration. 
		// Removing it now would cause errors in calling methods which catch this exception
		return pluginManager.getNormaliserManager().lookup(name);
	}

	/*
	 * ------------------------------------------------------------- NORMALISATION: NO NORMALISER SPECIFIED
	 * -------------------------------------------------------------
	 */

	/**
	 * Normalise the xena input source by getting the currently active directory that
	 * is set in the fileNamerManager, active fileNamer and active wrapper, and then 
	 * call: <code>normalise(XenaInputSource, File, FileNamer, XMLFilter)</code>
	 * Return the NormaliserDataStore that is returned as a result of the normalisation.
	 * 
	 * @param xis - the xena input source to be normalised
	 * @return A NormaliserDataStore object with the results of the normalisation.
	 * @throws XenaException in the case of an error occurring during the normalisation process.
	 * 
	 * @see au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager.getDestinationDir()
	 * 
	 */
	public NormaliserResults normalise(XenaInputSource xis) throws XenaException {

		File destinationDir = pluginManager.getFileNamerManager().getDestinationDir();
		if (destinationDir == null) {
			throw new XenaException("Null destination directory! Please ensure that the destination directory is set before normalising.");
		}
		AbstractFileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
		AbstractMetaDataWrapper wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
		NormaliserResults results = normalise(xis, destinationDir, fileNamer, wrapper);
		if (results != null) {
			return results;
		}
		throw new XenaException("No results returned!");
	}

	/**
	 * Normalise the xena input source to the destination directory, by 
	 *  getting the active fileNamer and wrapper, and then calling:
	 * <code>normalise(XenaInputSource, File, FileNamer, XMLFilter)</code>
	 * <p> 
	 * Return the NormaliserDataStore that is generated as a result of the
	 * normalisation.
	 * </p>
	 * <p>
	 * <b>NOTE</b> This method will update the destination directory for the fileNamerManager
	 * so that if any sub-packages are created during normalisation they will be output to the same
	 * location.
	 * </p>
	 * 
	 * @param xis - the XenaInputSource to normalise
	 * @param destinationDir - destination directory for the normalised files
	 * @return A NormaliserDataStore object with the results of the normalisation.
	 * @throws XenaException in the case of an error occurring during the normalisation process.
	 */
	public NormaliserResults normalise(XenaInputSource xis, File destinationDir) throws XenaException {
		pluginManager.getFileNamerManager().setDestinationDir(destinationDir);
		AbstractFileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
		AbstractMetaDataWrapper wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
		NormaliserResults results = normalise(xis, destinationDir, fileNamer, wrapper);
		if (results != null) {
			return results;
		}
		throw new XenaException("No results returned!");
	}

	/**
	 * Normalise the xena input source to the destination directory using the fileNamer
	 * and wrapper. If the XenaInputSource has not got a type set, then guess
	 * the type of the xis, and update the XenaInputSource type field. Then get 
	 * the appropriate normaliser based on the type of the XenaInputSource. 
	 * Then use the specified fileNamer, wrapper and destination directory to normalise the files. 
	 * Return a list of NormaliserDataStore objects for each xena input source.
	 * 
	 * <p>
	 * <b>NOTE</b> This method will update the destination directory for the fileNamerManager
	 * so that if any sub-packages are created during normalisation they will be output to the same
	 * location.
	 * </p>
	 * 
	 * @param xis - the XenaInputSource to normalise
	 * @param destinationDir - destination directory for the normalised files
	 * @param fileNamer - an instance of a FileNamer object to return the output file
	 * @param wrapper - an instance of an XMLFilter to 'wrap' the normalised data stream in meta data.
	 * @return A NormaliserDataStore object with the results of the normalisation.
	 * @throws XenaException in the case of an error occurring during the normalisation process.
	 */
	public NormaliserResults normalise(XenaInputSource xis, File destinationDir, AbstractFileNamer fileNamer, AbstractMetaDataWrapper wrapper)
	        throws XenaException {
		setDestinationDir(destinationDir);

		NormaliserResults results = new NormaliserResults(xis);

		if (xis.getType() == null) {
			// find the most likely type for this XIS...
			Guess bestGuess = null;
			try {
				bestGuess = getBestGuess(xis);
			} catch (IOException e) {
				e.printStackTrace();
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

	/*
	 * ------------------------------------------------------------- NORMALISATION: NORMALISER SPECIFIED
	 * -------------------------------------------------------------
	 */

	/**
	 * Normalise a list of XenaInputSources using a specified normaliser. For
	 * example, the binary normaliser :) Returns a list of NormaliserDataStore
	 * objects corresponding to each XenaInputSource.
	 * 
	 * <p><b>WARNING</b> It is possible a normaliser may require the type field
	 * in the XenaInputSource to be set. This should be done by the calling
	 * application. It is generally recommended that rather than specifying a normaliser,
	 * the type field in the XenaInputSource is set appropriately instead.</p>
	 * 
	 * @param   xis - the XenaInputSource to normalise
	 * @param   normaliser - a instance of a normaliser to use.
	 * @return  A NormaliserDataStore object with the results of the normalisation.
	 * @throws  XenaException in the case of an error occurring during the normalisation process.
	 */
	public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser) throws XenaException {
		File destinationDir = getDestinationDir();
		if (destinationDir == null) {
			throw new XenaException("Null destination directory! Please ensure that the destination directory is set before normalising.");
		}
		AbstractFileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
		AbstractMetaDataWrapper wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
		NormaliserResults results = new NormaliserResults(xis);
		try {
			results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
		} catch (IOException e) {
			throw new XenaException(e);
		}

		return results;
	}

	/**
	 * Normalise a XenaInputSource using a specified normaliser. For
	 * example, the binary normaliser :) Returns a NormaliserDataStore object with the results
	 * of the normalisation for a particular XenaInputSource. Send the output files to the 
	 * specified destination.
	 * 
	 * <p>
	 * <b>NOTE</b> This method will update the destination directory for the fileNamerManager
	 * so that if any sub-packages are created during normalisation they will be output to the same
	 * location.
	 * </p>
	 * 
	 * <p><b>WARNING</b> It is possible a normaliser may require the type field
	 * in the XenaInputSource to be set. This should be done by the calling
	 * application. It is generally recommended that rather than specifying a normaliser,
	 * the type field in the XenaInputSource is set appropriately instead.</p>
	 * 
	 * @param xis - the XenaInputSource to normalise
	 * @param normaliser - a instance of a normaliser to use.
	 * @param destinationDir - destination directory for the normalised files
	 * @return A NormaliserDataStore object with the results of the normalisation.
	 * @throws XenaException in the case of an error occurring during the normalisation process.
	 */
	public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
		setDestinationDir(destinationDir);
		NormaliserResults results = new NormaliserResults(xis);

		AbstractFileNamer fileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
		AbstractMetaDataWrapper wrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();

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
	 * <p>
	 * <b>NOTE</b> This method will update the destination directory for the fileNamerManager
	 * so that if any sub-packages are created during normalisation they will be output to the same
	 * location.
	 * </p>
	 * 
	 * <p><b>WARNING</b> It is possible a normaliser may require the type field
	 * in the XenaInputSource to be set. This should be done by the calling
	 * application. It is generally recommended that rather than specifying a normaliser,
	 * the type field in the XenaInputSource is set appropriately instead.</p>
	 * 
	 * @param xis - the XenaInputSource to normalise
	 * @param normaliser - a instance of a normaliser to use.
	 * @param destinationDir - destination directory for the normalised files
	 * @param fileNamer - an instance of a FileNamer object to return the output file
	 * @param wrapper - an instance of an XMLFilter to 'wrap' the normalised data stream in meta data.
	 * @return A NormaliserDataStore object with the results of the normalisation.
	 * @throws XenaException  in the case of an error occurring during the normalisation process.
	 */
	public NormaliserResults normalise(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir, AbstractFileNamer fileNamer,
	                                   AbstractMetaDataWrapper wrapper) throws XenaException {
		setDestinationDir(destinationDir);
		NormaliserResults results = new NormaliserResults(xis);
		try {
			results = pluginManager.getNormaliserManager().normalise(xis, normaliser, destinationDir, fileNamer, wrapper);
		} catch (IOException e) {
			throw new XenaException(e);
		}
		return results;
	}

	/*
	 * ------------------------------------------- -------------------------------------------
	 * ------------------------------------------- Export Functions -------------------------------------------
	 * ------------------------------------------- -------------------------------------------
	 */

	/**
	 * Export a Xena file to its original form. It is possible that a normalised file may not be able to
	 * be returned to it's original form, it is also possible that if it is exported some information may be lost.
	 * 
	 * <p>
	 * The built in binary normaliser is an example of a normaliser that will always return an exact copy of the
	 * original file.</p>
	 * 
	 * <p>
	 * An example of the first behaviour is the NAA office normaliser - since we don't know from which office
	 * application the office document originated, we are unable to export it to its original form.</p>
	 * 
	 * <p>
	 * An example of the second behaviour is the NAA image normaliser - if we take an image and normalise it
	 * we will end up with a PNG file, but during encoding some information may have been lost. If the file
	 * is exported, it is possible the resulting file will have a lower resolution or colour palette.</p>
	 * 
	 * 
	 * @param xis - A xena input source that is to be exported.
	 * @param destinationDir - the destination directory for the exported file.
	 * 
	 * @return ExportResult an object that contains all the information about the export.
	 * 
	 * @throws XenaException - Thrown if for some reason there is an error exporting. This may be from the following:<ul>
	 *      <li>IOException reading the xis parameter;</li>
	 *      <li>Error configuring the parser while exporting;</li>
	 *      <li>A SAXException occurring during the export process</li>
	 *      <li>A XenaException for some other reason, including there not being a denormaliser for this type,
	 *              or the Xena file not being recognised at all, or the output file already existing.</li></ul>
	 * 
	 * @see ExportResult
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

	/**
	 * Export a Xena file to it's original form. It is possible that a normalised file may not be able to
	 * be returned to it's original form, it is also possible that if it is exported some information may be lost.
	 * 
	 * <p>
	 * This method differs from the default export method in that it requires a flag to specify whether or not to
	 * overwrite files when we perform the export.</p>
	 * 
	 * @param xis - A xena input source that is to be exported.
	 * @param destinationDir - the destination directory for the exported file.
	 * 
	 * @return ExportResult an object that contains all the information about the export.
	 * 
	 * @throws XenaException - Thrown if for some reason there is an error exporting. This may be from the following:<ul>
	 *      <li>IOException reading the xis parameter;</li>
	 *      <li>Error configuring the parser while exporting;</li>
	 *      <li>A SAXException occurring during the export process</li>
	 *      <li>A XenaException for some other reason, including there not being a denormaliser for this type,
	 *              or the Xena file not being recognised at all.</li></ul>
	 * 
	 * @see ExportResult
	 * @see export(XenaInputSource xis, File destinationDir)
	 */
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

	/**
	 * 
	 * Export a Xena file to it's original form. It is possible that a normalised file may not be able to
	 * be returned to it's original form, it is also possible that if it is exported some information may be lost.
	 * 
	 * <p>
	 * This method differs from the default export method in that it requires a flag to specify whether or not to
	 * overwrite files when we perform the export.</p>
	 * 
	 * @param xis - A xena input source that is to be exported.
	 * @param destinationDir - the destination directory for the exported file.
	 * @param outputFileName
	 * @param destinationDir
	 * 
	 * @return ExportResult an object containing all the information about the export.
	 * 
	 * @throws XenaException - Thrown if for some reason there is an error exporting. This may be from the following:<ul>
	 *      <li>IOException reading the xis parameter;</li>
	 *      <li>Error configuring the parser while exporting;</li>
	 *      <li>A SAXException occurring during the export process</li>
	 *      <li>A XenaException for some other reason, including there not being a denormaliser for this type,
	 *              or the Xena file not being recognised at all.</li></ul>
	 * 
	 * @see export(XenaInputSource xis, File destinationDir)
	 * 
	 */
	public ExportResult export(XenaInputSource xis, File destinationDir, String outputFileName, boolean overwrite) throws XenaException {
		try {
			return pluginManager.getNormaliserManager().export(xis, destinationDir, outputFileName, overwrite);
		} catch (IOException iox) {
			throw new XenaException(iox);
		} catch (ParserConfigurationException pce) {
			throw new XenaException(pce);
		} catch (SAXException sx) {
			throw new XenaException(sx);
		}
	}

	/*
	 * ------------------------------------------- -------------------------------------------
	 * ------------------------------------------- Miscellaneous Functions -------------------------------------------
	 * ------------------------------------------- -------------------------------------------
	 */

	/**
	 * Returns a set of XISs which have been classified as 'children' by
	 * the filters loaded in BatchFilterManager. Child XISs will be
	 * normalised as part of the normalising process for their parent,
	 * and thus should not be normalised separately.
	 * 
	 * @param xisColl
	 * @return The map that contains the XIS's that will be embedded into the
	 * normalised object produced by normalisation of the parent XIS. It also contains
	 * a NormaliserResults object for the child XISs as the value component of each element.
	 * @throws XenaException
	 */
	public Map<XenaInputSource, NormaliserResults> getChildren(Collection<XenaInputSource> xisColl) {
		return pluginManager.getBatchFilterManager().getChildren(xisColl);
	}

}
