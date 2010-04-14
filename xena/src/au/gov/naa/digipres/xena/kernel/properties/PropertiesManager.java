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
 * Created on 10/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * LoadManager for properties. This class is used to initialise and return the list of PluginProperties.
 * It also handles saving and loading individual properties to and from Java Preferences. 
 * 
 * created 10/02/2006
 * xena
 */
public class PropertiesManager {
	private Preferences prefs;
	private List<PluginProperties> allPluginProperties = new ArrayList<PluginProperties>();

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
	 * Adds the given list of PluginProperties to the PropertiesManager
	 * @param pluginPropertiesList
	 */
	public void addPluginProperties(List<PluginProperties> pluginPropertiesList) {
		for (PluginProperties pluginProps : pluginPropertiesList) {
			pluginProps.setManager(this);
			pluginProps.initialiseProperties();
			allPluginProperties.add(pluginProps);
		}
	}

	/**
	 * @return Returns the list of pluginProperties.
	 */
	public List<PluginProperties> getPluginProperties() {
		return allPluginProperties;
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

}
