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

package au.gov.naa.digipres.xena.kernel.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.DeSerializeClassLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilter;
import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilterManager;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.TypePrinter;
import au.gov.naa.digipres.xena.kernel.type.TypePrinterManager;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * This class is repsonsible for managing the loading of plugins in to Xena.
 * 
 * 
 * 
 * 
 * @created April 24, 2002
 */
public class PluginManager {

	private Xena xena;

	/*
	 * These are the component managers that for this plugin Manager.
	 */
	private TypeManager typeManager;
	private GuesserManager guesserManager;
	private NormaliserManager normaliserManager;
	private MetaDataWrapperManager metaDataWrapperManager;
	private FileNamerManager fileNamerManager;
	private TypePrinterManager typePrinterManager;
	private BatchFilterManager batchFilterManager;
	private ViewManager viewManager;
	private PropertiesManager propertiesManager;

	/**
	 * The deserialised class loader
	 */
	private DeSerializeClassLoader deserClassLoader = new DeSerializeClassLoader(getClass().getClassLoader());

	/**
	 * A list of all the names of the plugins that have been loaded already.
	 */
	private List<XenaPlugin> loadedPlugins = new ArrayList<XenaPlugin>();

	/**
	 * A list of all the names of plugins that could not be loaded.
	 */
	private ArrayList<XenaPlugin> unloadablePlugins = new ArrayList<XenaPlugin>();

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
	public PluginManager(Xena xena) {
		this.xena = xena;

		// Each of the different types of classes is loaded and managed by a
		// Manager class. Here we enumerate all the Managers.
		typeManager = new TypeManager(this);
		guesserManager = new GuesserManager(this);
		normaliserManager = new NormaliserManager(this);
		metaDataWrapperManager = new MetaDataWrapperManager(this);
		fileNamerManager = new FileNamerManager(this);
		typePrinterManager = new TypePrinterManager(this);
		batchFilterManager = new BatchFilterManager(this);
		viewManager = new ViewManager(this);
		propertiesManager = new PropertiesManager(this);

	}

	/**
	 * @return the xena
	 */
	public Xena getXena() {
		return xena;
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
	public static String getPluginClassName(File pluginFile) throws IOException, MalformedURLException {
		URL url = pluginFile.toURI().toURL();
		// we don't use deserClassLoader here because it would
		// have conflicting name.properties
		URLClassLoader cl = new URLClassLoader(new URL[] {url}, null);
		InputStream is = cl.getResourceAsStream("name.properties");
		if (is == null) {
			throw new IOException("Cannot find name.properties in plugin " + pluginFile);
		}
		Properties namep = new Properties();
		namep.load(is);
		String name = namep.getProperty("classname");
		return name;
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

		if (pluginLocation.isDirectory()) {
			// handle directory -> add any jar files to our file list.
			File[] list = pluginLocation.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".jar");
				}
			});
			for (File element : list) {
				pluginFiles.add(element);
			}
		} else if (pluginLocation.isFile()) {
			// handle single file -> if its a jar file, add it to our file list.
			String theFilename = pluginLocation.getName();
			String extension = "";
			int whereDot = theFilename.lastIndexOf('.');
			if (0 < whereDot && whereDot <= theFilename.length() - 2) {
				extension = theFilename.substring(whereDot + 1);
			}
			if ("jar".equals(extension)) {
				pluginFiles.add(pluginLocation);
			}
		}

		// go through our plugin files list and try to add them to the class path,
		// and get the name from the name.properties file and add it to our plugin names list.
		for (File pluginFile : pluginFiles) {
			String pluginClassName = getPluginClassName(pluginFile);
			try {
				deserClassLoader.addURL(pluginFile.toURI().toURL());

				Class<?> pluginClass = Class.forName(pluginClassName, true, deserClassLoader);
				XenaPlugin xenaPlugin = (XenaPlugin) pluginClass.newInstance();
				loadPlugin(xenaPlugin);
			} catch (MalformedURLException malformedURLException) {
				// hmmm.... is this useful or necessary?
				// TODO - handle this better.
				malformedURLException.printStackTrace();
			} catch (ClassNotFoundException e) {
				throw new XenaException("Problem loading plugin", e);
			} catch (InstantiationException e) {
				throw new XenaException("Problem loading plugin", e);
			} catch (IllegalAccessException e) {
				throw new XenaException("Problem loading plugin", e);
			}
		}
	}

	public void loadPlugins(List<String> classNameList) throws XenaException {
		for (String className : classNameList) {
			try {
				XenaPlugin xenaPlugin = (XenaPlugin) Class.forName(className).newInstance();
				loadPlugin(xenaPlugin);
			} catch (ClassNotFoundException e) {
				throw new XenaException("Problem loading plugin", e);
			} catch (InstantiationException e) {
				throw new XenaException("Problem loading plugin", e);
			} catch (IllegalAccessException e) {
				throw new XenaException("Problem loading plugin", e);
			}
		}
	}

	/**
	 * @param xenaPlugin
	 * @throws XenaException 
	 */
	private void loadPlugin(XenaPlugin xenaPlugin) throws XenaException {
		// Types
		List<Type> typeList = xenaPlugin.getTypes();
		if (typeList != null && !typeList.isEmpty()) {
			typeManager.addTypes(typeList);
		}

		// Guessers
		List<Guesser> guesserList = xenaPlugin.getGuessers();
		if (guesserList != null && !guesserList.isEmpty()) {
			guesserManager.addGuessers(guesserList);
		}

		// Normalisers
		Map<Object, Set<Type>> inputMap = xenaPlugin.getNormaliserInputMap();
		Map<Object, Set<Type>> outputMap = xenaPlugin.getNormaliserOutputMap();
		if (inputMap != null && !inputMap.isEmpty() && outputMap != null && !outputMap.isEmpty()) {
			normaliserManager.addNormaliserMaps(inputMap, outputMap);
		}

		// Metadata Wrappers
		Map<AbstractMetaDataWrapper, XMLFilter> wrapperMap = xenaPlugin.getMetaDataWrappers();
		if (wrapperMap != null && !wrapperMap.isEmpty()) {
			metaDataWrapperManager.addMetaDataWrappers(wrapperMap);
		}

		// File namers
		List<AbstractFileNamer> fileNamerList = xenaPlugin.getFileNamers();
		if (fileNamerList != null && !fileNamerList.isEmpty()) {
			fileNamerManager.addFileNamers(fileNamerList);
		}

		// Type printers
		List<TypePrinter> typePrinterList = xenaPlugin.getTypePrinters();
		if (typePrinterList != null && !typePrinterList.isEmpty()) {
			typePrinterManager.addTypePrinters(typePrinterList);
		}

		// Batch filters
		List<BatchFilter> batchFilterList = xenaPlugin.getBatchFilters();
		if (batchFilterList != null && !batchFilterList.isEmpty()) {
			batchFilterManager.addBatchFilters(batchFilterList);
		}

		// Views
		List<XenaView> viewList = xenaPlugin.getViews();
		if (viewList != null && !viewList.isEmpty()) {
			viewManager.addViews(viewList);
		}

		// Plugin properties
		List<PluginProperties> pluginPropertiesList = xenaPlugin.getPluginPropertiesList();
		if (pluginPropertiesList != null && !pluginPropertiesList.isEmpty()) {
			propertiesManager.addPluginProperties(pluginPropertiesList);
		}

		loadedPlugins.add(xenaPlugin);
		logger.fine("Successfully loaded the " + xenaPlugin.getName() + " (" + xenaPlugin.getVersion() + ") plugin");
	}

	// *******************************
	// * *
	// * GETTERS AND SETTERS FOLLOW *
	// * *
	// *******************************

	/**
	 * Return the string representation of the plugin manager. At this stage the
	 * plugin manager string representation is simply.... "Plugin manager!"
	 * followed by a list of loaded plugins.
	 * @return The string representation of the plugin manager.
	 */
	@Override
	public String toString() {
		StringBuffer returnBuffer = new StringBuffer("Plugin manager!");
		returnBuffer.append(System.getProperty("line.separator"));
		returnBuffer.append("Loaded plugins:");
		returnBuffer.append(System.getProperty("line.separator"));
		for (Object element : getLoadedPlugins()) {
			returnBuffer.append(element);
			returnBuffer.append(System.getProperty("line.separator"));
		}
		return new String(returnBuffer);
	}

	/**
	 * Return the list of loaded plugins.
	 * @return Returns the list of loaded plugins.
	 */
	public List<XenaPlugin> getLoadedPlugins() {
		return loadedPlugins;
	}

	/**
	 * @return Returns the unloadablePlugins.
	 */
	public ArrayList<XenaPlugin> getUnloadablePlugins() {
		return unloadablePlugins;
	}

	/**
	 * @return Returns the batchFilterManager.
	 */
	public BatchFilterManager getBatchFilterManager() {
		return batchFilterManager;
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
	public PropertiesManager getPropertiesManager() {
		return propertiesManager;
	}

}
