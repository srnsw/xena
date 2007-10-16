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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;

/**
 * Abstract class representing the set of properties for a plugin. Each plugin with properties
 * to set should create an extension of this class, and implement the getName, getProperties and
 * initialiseProperties methods.
 * 
 * The getProperties method can be used to retrieve a List of XenaProperties which can be loaded and
 * saved individually, or the getPropertiesDialog method can be used to retrieve a dialog containing
 * input fields and buttons etc which will display and save updated properties automatically.
 * 
 * created 10/04/2006
 * xena
 * Short desc of class:
 */
public abstract class PluginProperties {
	private PropertiesManager manager;

	/**
	 * Returns the name of this properties set. Will generally just be the name of the plugin.
	 * @return properties set name
	 */
	public abstract String getName();

	/**
	 * Returns a List of XenaProperties representing the property set for this plugin.
	 * @return
	 */
	public abstract List<XenaProperty> getProperties();

	/**
	 * Initialises the property set for this plugin. This method is called by PropertiesManager
	 * after each PluginProperties object has been first created.
	 *
	 */
	public abstract void initialiseProperties();

	/**
	 * Returns a dialog which will display a method for modifying the property value for each
	 * property (for example a simple text entry field for String properties, a text entry field
	 * and a Browse button for File properties, etc). The dialog will be a child of the given
	 * parent Window, which must be either a Frame or a Dialog.
	 * @param parent Frame or Window
	 * @return PropertiesDialog representing the property set for this plugin
	 */
	public PropertiesDialog getPropertiesDialog(Window parent) {
		PropertiesDialog dialog;
		if (parent instanceof Frame) {
			dialog = new PropertiesDialog((Frame) parent, getProperties(), manager, "Properties for " + getName());
		} else if (parent instanceof Dialog) {
			dialog = new PropertiesDialog((Dialog) parent, getProperties(), manager, "Properties for " + getName());
		} else {
			throw new IllegalArgumentException("Developer error - parent must be either a Frame or a Dialog");
		}
		return dialog;
	}

	/**
	 * Return the value of the given property name for this plugin.
	 * @param propertyName
	 * @return property value
	 */
	public String getPropertyValue(String propertyName) {
		return manager.getPropertyValue(this.getName(), propertyName);
	}

	/**
	 * @return Returns the properties manager.
	 */
	public PropertiesManager getManager() {
		return manager;
	}

	/**
	 * @param manager The properties manager to set.
	 */
	public void setManager(PropertiesManager manager) {
		this.manager = manager;
	}
}
