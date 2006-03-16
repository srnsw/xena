package au.gov.naa.digipres.xena.kernel.type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

public class TypePrinterManager implements LoadManager {
	Map typeToPrinter = new HashMap();
	
    private PluginManager pluginManager;
	
	protected List guessers = new ArrayList();

	public TypePrinterManager(PluginManager pluginManager) {
	    this.pluginManager = pluginManager;
    }

//	static TypePrinterManager theSingleton = new TypePrinterManager();
//	public static TypePrinterManager singleton() {
//		return theSingleton;
//	}

	/**
	 * complete
	 *
	 * @throws XenaException
	 * @todo Implement this xena.kernel.LoadManager method
	 */
	public void complete() throws XenaException {
	}

	/**
	 * Load classes from a plugin.
	 *
	 * @param preferences The preferences file which describes this plugin.
	 * @return Whether anything was successfully loaded.
	 * @throws XenaException
	 * @todo Implement this xena.kernel.LoadManager method
	 */
	public boolean load(JarPreferences preferences) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(preferences);
			List instances = loader.loadInstances("typePrinters");
			Iterator it = instances.iterator();
			while (it.hasNext()) {
				TypePrinter tp = (TypePrinter)it.next();
                tp.setTypePrinterManager(this);
				typeToPrinter.put(tp.getType(), tp);
			}
			return!typeToPrinter.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	public TypePrinter lookup(XenaFileType type) {
		return (TypePrinter)typeToPrinter.get(type);
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
