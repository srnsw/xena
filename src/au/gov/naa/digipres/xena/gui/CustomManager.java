package au.gov.naa.digipres.xena.gui;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Class for managing and loading Custom menu items.
 * @author Chris Bitmead
 */
public class CustomManager implements LoadManager {
	protected static CustomManager theSingleton = new CustomManager();

	/**
	 * List of Custom menus, ordered by menu ranking.
	 */
	protected List customsByMenu = new ArrayList();

	/**
	 * List of Custom menus, ordered by toolbar ranking.
	 */
	protected List customsByToolbar = new ArrayList();

	/**
	 * Map of Custom menus keyed by name.
	 */
	protected Map customsByName = new HashMap();

	/**
	 * Map of Custom menus keyed by class object.
	 */
	protected Map customsByClass = new HashMap();

	/**
	 * Map of Custom menus keyed by class name.
	 */
	protected Map customsByClassName = new HashMap();

	public CustomManager() {
	}

	public static CustomManager singleton() {
		return theSingleton;
	}

	public Collection getAllByMenu() {
		return customsByMenu;
	}

	public Collection getAllByToolbar() {
		return customsByToolbar;
	}

	protected void addCustom(CustomMenuItem custom) {
		customsByMenu.add(custom);
		customsByToolbar.add(custom);
		// Menu place holders are null
		if (custom.getMenuItem() != null) {
			customsByName.put(custom.getMenuItem().getText(), custom);
		}
		customsByClass.put(custom.getClass(), custom);
		customsByClassName.put(custom.getClass().getName(), custom);
	}

	public CustomMenuItem lookupByName(String name) throws XenaException {
		if (!customsByName.containsKey(name)) {
			throw new XenaException("Custom Not Found");
		}
		return (CustomMenuItem)customsByName.get(name);
	}

	public CustomMenuItem lookupByClass(Class cls) throws XenaException {
		if (!customsByClass.containsKey(cls)) {
			throw new XenaException("Custom Not Found");
		}
		return (CustomMenuItem)customsByClass.get(cls);
	}

	public CustomMenuItem lookupByClassName(String cls) throws XenaException {
		if (!customsByClassName.containsKey(cls)) {
			throw new XenaException("Custom Not Found");
		}
		return (CustomMenuItem)customsByClassName.get(cls);
	}

	public boolean load(JarPreferences props) throws XenaException {
        
 
        
        
		try {
			PluginLoader loader = new PluginLoader(props);
			List customs = loader.loadInstances("customs");

			// Install all the Custom menu functions.
			Iterator it = customs.iterator();
			while (it.hasNext()) {
				Object o = it.next();
                  
				if (!(o instanceof CustomMenuItem)) {
					throw new XenaException("Class: " + o.getClass().getName() + " does not inherit from " + CustomMenuItem.class.getName());
				}
				CustomMenuItem custom = (CustomMenuItem)o;
				addCustom(custom);
			}
			// Make sure that the menu items come out in the right order
			Collections.sort(customsByMenu, new Comparator() {
				public int compare(Object o1, Object o2) {
					CustomMenuItem c1 = (CustomMenuItem)o1;
					CustomMenuItem c2 = (CustomMenuItem)o2;
					int diff = c1.getPath().size() - c2.getPath().size();
					if (diff != 0) {
						return diff;
					}
					return c1.getMenuRanking() - c2.getMenuRanking();
				}
			});
			// Make sure that the tool bar buttons come out in the right order
			Collections.sort(customsByToolbar, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((CustomMenuItem)o1).getToolbarRanking() - ((CustomMenuItem)o2).getToolbarRanking();
				}
			});
			return!customs.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	public void complete() {
	}
}
