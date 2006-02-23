/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * @author justinw5
 * created 10/02/2006
 * xena
 * Short desc of class:
 */
public class PropertiesManager implements LoadManager
{
	private Preferences prefs;
	private List<PluginProperties> pluginProperties = 
		new ArrayList<PluginProperties>();
	
	/**
	 * 
	 */
	public PropertiesManager()
	{
		prefs = Preferences.userNodeForPackage(this.getClass());
	}

    public boolean load(JarPreferences props) throws XenaException {
        try {
            PluginLoader loader = new PluginLoader(props);
            List<PluginProperties> instances = loader.loadInstances("properties");
            for (PluginProperties pluginProps : instances)
            {
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

	public void complete() throws XenaException
	{
	}

	/**
	 * @return Returns the pluginProperties.
	 */
	public List<PluginProperties> getPluginProperties()
	{
		return pluginProperties;
	}
	
	public void saveProperty(XenaProperty property)
	{
		String key = getPreferencesKey(property.getPluginName(), property.getName());
		prefs.put(key, property.getValue());		
	}
	
	public void loadProperty(XenaProperty property)
	{
		String key = getPreferencesKey(property.getPluginName(), property.getName());
		property.setValue(prefs.get(key, ""));
	}
	
	public String getPropertyValue(String pluginName, String propertyName)
	{
		return prefs.get(getPreferencesKey(pluginName, propertyName), "");
	}
	
	/**
	 * Gets the key to be used when saving or loading this property from
	 * Preferences
	 * 
	 * @return preferences key
	 */
	private static String getPreferencesKey(String pluginName, String propertyName)
	{
		return pluginName + "/" + propertyName;
	}
}
