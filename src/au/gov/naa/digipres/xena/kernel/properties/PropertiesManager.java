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

/*
 * Created on 10/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.LoadManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * LoadManager for properties. This class is used to initialise and return the list of PluginProperties.
 * It also handles saving and loading individual properties to and from Java Preferences. 
 * 
 * created 10/02/2006
 * xena
 * Short desc of class:
 */
public class PropertiesManager implements LoadManager {
	private Preferences prefs;
	private List<PluginProperties> pluginProperties = new ArrayList<PluginProperties>();

	private PluginManager pluginManager;

	/**
	 * Create a new PropertiesManager object
	 * @param pluginManager
	 */
	public PropertiesManager(PluginManager pluginManager) {
		prefs = Preferences.userNodeForPackage(this.getClass());
		this.pluginManager = pluginManager;
	}

	/**
	 * Create and initialise the PluginProperties objects for this application.
	 * A plugin will have a PluginProperties object if it is defined in the 
	 * preferences.properties file for that plugin.
	 */
	public boolean load(JarPreferences props) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(props);
			List<PluginProperties> instances = loader.loadInstances("properties");
			for (PluginProperties pluginProps : instances) {
				pluginProps.setManager(this);
				pluginProps.initialiseProperties();
			}
			pluginProperties.addAll(instances);
			return !instances.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	public void complete() throws XenaException {
	}

	/**
	 * @return Returns the list of pluginProperties.
	 */
	public List<PluginProperties> getPluginProperties() {
		return pluginProperties;
	}

	/**
	 * Save the given XenaProperty to Java Preferences. The getPreferencesKey
	 * method is used to determine the preferences key to use.
	 * @param property
	 */
	public void saveProperty(XenaProperty property) {
		String key = getPreferencesKey(property.getPluginName(), property.getName());
		prefs.put(key, property.getValue());
	}

	/**
	 * Populate the given XenaProperty with the stored value in Java Preferences. 
	 * The getPreferencesKey method is used to determine the preferences key to use.
	 * @param property
	 */
	public void loadProperty(XenaProperty property) {
		String key = getPreferencesKey(property.getPluginName(), property.getName());
		property.setValue(prefs.get(key, ""));
	}

	/**
	 * Return the stored Java Preferences value for the given property of the given plugin.
	 * The getPreferencesKey method is used to determine the preferences key to use.
	 * @param pluginName
	 * @param propertyName
	 * @return String value which represents the property.
	 */
	public String getPropertyValue(String pluginName, String propertyName) {
		return prefs.get(getPreferencesKey(pluginName, propertyName), "");
	}

	/**
	 * Gets the key to be used when saving or loading this property from
	 * Preferences. 
	 * 
	 * @return preferences key
	 */
	private static String getPreferencesKey(String pluginName, String propertyName) {
		return pluginName + "/" + propertyName;
	}
}
