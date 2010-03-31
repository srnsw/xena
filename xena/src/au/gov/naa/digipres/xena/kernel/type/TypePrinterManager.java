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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

public class TypePrinterManager {
	Map<XenaFileType, TypePrinter> typeToPrinter = new HashMap<XenaFileType, TypePrinter>();

	private PluginManager pluginManager;

	public TypePrinterManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void addTypePrinters(List<TypePrinter> typePrinterList) throws XenaException {
		for (TypePrinter typePrinter : typePrinterList) {
			typePrinter.setTypePrinterManager(this);
			typeToPrinter.put(typePrinter.getType(), typePrinter);
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
