package au.gov.naa.digipres.xena.gui;
import java.awt.Container;

/**
 * Menus for Xena "View" menus and context menus.
 * Helps keep the "View" and context menus in sync.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public abstract class XenaMenu {
	/**
	 * Ensures that menus are in sync with available InternalFrame types
	 *
	 * @param  menus  menus avaliable for display
	 */
	public static void syncAll(XenaMenu[] menus) {
		for (int i = 0; i < menus.length; i++) {
			menus[i].sync();
		}
	}

	/**
	 * Initialises listeners associated with available menus
	 *
	 * @param  menus  menus avaliable for display
	 */
	public static void initListenersAll(XenaMenu[] menus) {
		for (int i = 0; i < menus.length; i++) {
			menus[i].initListeners();
		}
	}

	/**
	 * Constructs the menu tree.
	 * Will be called once for View menu and once for context menu.
	 *
	 * @param  component Top level menu to add the tree to.
	 */
	public void makeMenu(Container component) {
	}

	/**
	 * Synchronize the menus.
	 */
	public void sync() {
	}

	/**
	 * Initialize the menu listeners.
	 */
	public void initListeners() {
	}
}
