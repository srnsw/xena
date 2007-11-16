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

package au.gov.naa.digipres.xena.kernel.filenamer;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 */
public class FileNamerManager {
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
	 * Indicates that a file namer has not been automatically loaded from a plugin, or set using setActiveFileNamer.
	 * This removes a potential problem where the user manually selects the DefaultFileNamer, but then loads a new
	 * plugin with a FileNamer which would overwrite the DefaultFileNamer.
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

	public void addFileNamers(List<AbstractFileNamer> fileNamerList) {
		for (AbstractFileNamer namer : fileNamerList) {
			namer.setFileNamerManager(this);
			namers.put(namer.getClass().getName(), namer);

			if (activeFileNamerUnchanged) {
				activeFileNamer = namer;
				activeFileNamerUnchanged = false;
			}

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
		if (!destinationDirectory.exists() || !destinationDirectory.isDirectory()) {
			throw new IllegalArgumentException("Destination directory must be a valid directory!");
		}
		destinationDir = destinationDirectory;
	}

}
