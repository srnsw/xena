package au.gov.naa.digipres.xena.gui;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Class to manage GuiConfigureNormaliser instances.
 * @author     Chris Bitmead
 * @created    April 16, 2002
 */
public class GuiConfigureNormaliserManager implements LoadManager {
	static GuiConfigureNormaliserManager theSingleton = new GuiConfigureNormaliserManager();

	protected Map normaliserToConfigure = new HashMap();

	public GuiConfigureNormaliserManager() {
	}

	public static GuiConfigureNormaliserManager singleton() {
		return theSingleton;
	}

	/**
	 *  Loads all GUI normaliser configurers as listed in the plugin.properties
	 *  files loaded at runtime
	 *
	 * @param  props                properties class loaded at runtime
	 * @exception  XenaException
	 */
	public boolean load(JarPreferences props) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(props);
			List configs = loader.loadInstances("guiConfigureNormalisers");
			Iterator it = configs.iterator();
			while (it.hasNext()) {
				GuiConfigureNormaliser config = (GuiConfigureNormaliser)it.next();
				add(config);
			}
			return!configs.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	/**
	 *  Returns instance of GuiConfigureNormaliser class associated with normaliser
	 *
	 * @param  normaliser           Name of normaliser
	 * @return                      GuiConfigureNormaliser associated with
	 *      normaliser
	 * @exception  XenaException  Description of Exception
	 */

	public GuiConfigureNormaliser lookup(Class normaliser, Type type) throws XenaException {
		try {
			GuiConfigureNormaliser rtn = (GuiConfigureNormaliser)normaliserToConfigure.get(new ConfigKey(normaliser, type));
			if (rtn == null) {
				rtn = (GuiConfigureNormaliser)normaliserToConfigure.get(new ConfigKey(normaliser, null));
			}
			if (rtn != null) {
				rtn = (GuiConfigureNormaliser)rtn.getClass().newInstance();
			}
			return rtn;
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	public boolean configure(XMLReader normaliser, XenaInputSource xis) throws XenaException, IOException {
		GuiConfigureNormaliser configure = lookup(normaliser.getClass(), xis == null ? null : xis.getType());
		if (configure != null) {
			JDialog dialog = new JDialog(MainFrame.singleton());
			dialog.setTitle("Configure");
			dialog.setModal(true);
			GuiConfigurePanel panel = new GuiConfigurePanel(dialog, normaliser, xis, configure);
			dialog.getContentPane().add(panel);
			dialog.setJMenuBar(panel.getMenuBar());
			MainFrame.packAndPosition(dialog);
			dialog.setVisible(true);
			if (panel.getSuccess()) {
				configure.finish(panel.activated);
			}
			return panel.getSuccess();
		}
		return true;
	}

	/**
	 *  Adds a GuiConfigureNormaliser to the list of accesable
	 *  GuiConfigureNormaliser's for a given normaliser
	 *
	 * @param  config  Description of Parameter
	 */
	protected void add(GuiConfigureNormaliser config) throws XenaException {
		normaliserToConfigure.put(new ConfigKey(config.normaliserClass(), config.normaliserType()), config);
	}

	class ConfigKey {
		Class normaliser;

		Type type;

		public ConfigKey(Class normaliser, Type type) {
			this.normaliser = normaliser;
			this.type = type;
		}

		public boolean equals(Object o) {
			boolean rtn = normaliser.equals(((ConfigKey)o).normaliser);
			if (rtn) {
				Type type2 = ((ConfigKey)o).type;
				if (type == null && type2 == null) {
					rtn = true;
				} else if (type == null || type2 == null) {
					rtn = false;
				} else {
					rtn = type.equals(((ConfigKey)o).type);
				}
			}
			return rtn;
		}

		public int hashCode() {
			int rtn = normaliser.hashCode();
			if (type != null) {
				rtn += type.hashCode();
			}
			return rtn;
		}
	}

	public void complete() {}
}
