/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;

public abstract class PluginProperties
{
	private PropertiesManager manager;
	
	public abstract String getName();	
	public abstract List<XenaProperty> getProperties();
	public abstract void initialiseProperties();
	
	public PropertiesDialog getPropertiesDialog(Window parent)
	{
		PropertiesDialog dialog;
		if (parent instanceof Frame)
		{
			dialog = new PropertiesDialog((Frame)parent,
			                              getProperties(),
			                              manager,
			                              "Properties for " + getName());
		}
		else if (parent instanceof Dialog)
		{
			dialog = new PropertiesDialog((Dialog)parent,
			                              getProperties(),
			                              manager,
			                              "Properties for " + getName());
		}
		else
		{
			throw new IllegalArgumentException("Developer error - parent must be either a Frame or a Dialog");
		}
		return dialog;
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
