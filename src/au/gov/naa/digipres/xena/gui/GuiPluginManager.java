package au.gov.naa.digipres.xena.gui;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;

/**
 *  An extension of Plugins which also loads gui related components.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class GuiPluginManager extends PluginManager {
	
    private ViewManager viewManager = ViewManager.singleton();
    private HelpManager helpManager = HelpManager.singleton();
    private GuiConfigureNormaliserManager guiConfigureNormaliserManager = GuiConfigureNormaliserManager.singleton();
    private CustomManager customManager = CustomManager.singleton();
    
    private GuiPluginManager() {
		loadManagers.add(HelpManager.singleton());
		loadManagers.add(GuiConfigureNormaliserManager.singleton());
		loadManagers.add(CustomManager.singleton());
	}

	/**
	 * @return    a singleton class for this class
	 */
	public static PluginManager singleton() {
		synchronized (PluginManager.class) {
			if (theSingleton == null) {
				theSingleton = new GuiPluginManager();
			}
		}
		return theSingleton;
	}
}
