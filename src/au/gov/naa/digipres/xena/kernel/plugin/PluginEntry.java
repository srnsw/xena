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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * A class to handle a plugin entry.
 * TODO: fix this description.
 * 
 * @create sep 2005
 */
class PluginEntry {

	private final PluginManager manager;
	String name;
	boolean loaded; // TODO: IS THIS DEPRECATED? (loaded attribute of plugin entry)
	ArrayList dependancyList;

	public PluginEntry(PluginManager manager, String name) throws XenaException {
		this.manager = manager;
		this.name = name;
		loaded = false;
		dependancyList = new ArrayList();

		// okay, time to get our dependencies from our jar file. this also serves to see
		// if our plugin entry can be created.
		JarPreferences root = JarPreferences.userRoot();
		try {
			// Check if the preferences file exists
			if (!root.jarNodeExists(name, this.manager.getDeserClassLoader())) {
				throw new XenaException("Plugin: " + name + " does not contain properties");
			}
		} catch (BackingStoreException ex) {
			throw new XenaException(ex);
		}
		JarPreferences preferences = root.node(name, this.manager.getDeserClassLoader());
		dependancyList = (ArrayList) preferences.getList("dependancies", dependancyList);

	}

	@Override
    public String toString() {
		StringBuffer returnBuffer = new StringBuffer("");
		returnBuffer.append("Name:" + name);
		returnBuffer.append(System.getProperty("line.separator"));
		returnBuffer.append("laoded:" + loaded);
		returnBuffer.append(System.getProperty("line.separator"));
		returnBuffer.append("dependancy list:");
		returnBuffer.append(System.getProperty("line.separator"));
		if (dependancyList.size() == 0) {
			returnBuffer.append("EMTPY");
			returnBuffer.append(System.getProperty("line.separator"));
		} else {
			for (Iterator iter = dependancyList.iterator(); iter.hasNext();) {
				returnBuffer.append(iter.next().toString());
				returnBuffer.append(System.getProperty("line.separator"));
			}
		}
		return new String(returnBuffer);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the loaded.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @param loaded The loaded to set.
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	/**
	 * @return Returns the dependancyList.
	 */
	public ArrayList getDependancyList() {
		return dependancyList;
	}
}
