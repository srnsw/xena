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

package au.gov.naa.digipres.xena.kernel.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.LoadManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

public class TypePrinterManager implements LoadManager {
	Map<XenaFileType, TypePrinter> typeToPrinter = new HashMap<XenaFileType, TypePrinter>();

	private PluginManager pluginManager;

	protected List guessers = new ArrayList();

	public TypePrinterManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

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
				TypePrinter tp = (TypePrinter) it.next();
				tp.setTypePrinterManager(this);
				typeToPrinter.put(tp.getType(), tp);
			}
			return !typeToPrinter.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	public TypePrinter lookup(XenaFileType type) {
		return typeToPrinter.get(type);
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
