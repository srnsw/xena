/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.Frame;
import java.util.List;

public abstract class PluginProperties
{
	private PropertiesManager manager;
	
	public abstract String getName();	
	public abstract List<XenaProperty> getProperties();
	public abstract void initialiseProperties();
	
	public PropertiesDialog getPropertiesDialog(Frame parent)
	{
		return new PropertiesDialog(parent,
		                            getProperties(),
		                            manager,
		                            "Properties for " + getName());
	}

	
	public String getPropertyValue(String propertyName)
	{
		return manager.getPropertyValue(this.getName(), propertyName);
	}

	
	
	/**
	 * @return Returns the manager.
	 */
	public PropertiesManager getManager()
	{
		return manager;
	}
	/**
	 * @param manager The manager to set.
	 */
	public void setManager(PropertiesManager manager)
	{
		this.manager = manager;
	}
}
