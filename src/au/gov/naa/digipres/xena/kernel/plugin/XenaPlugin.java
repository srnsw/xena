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
 */

package au.gov.naa.digipres.xena.kernel.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.XMLFilter;

import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilter;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypePrinter;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * @author Justin Waddell
 *
 */
public abstract class XenaPlugin implements Comparable<XenaPlugin> {

	/**
	 * Default method which returns an empty list
	 */
	public List<Type> getTypes() {
		return new ArrayList<Type>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<Guesser> getGuessers() {
		return new ArrayList<Guesser>();
	}

	/**
	 * Default method which returns an empty map
	 */
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		return new HashMap<Object, Set<Type>>();
	}

	/**
	 * Default method which returns an empty map
	 */
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		return new HashMap<Object, Set<Type>>();
	}

	/**
	 * Default method which returns an empty map
	 */
	public Map<Type, AbstractNormaliser> getTextNormaliserMap() {
		return new HashMap<Type, AbstractNormaliser>();
	}

	/**
	 * Default method which returns an empty map
	 */
	public Map<AbstractMetaDataWrapper, XMLFilter> getMetaDataWrappers() {
		return new HashMap<AbstractMetaDataWrapper, XMLFilter>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<AbstractFileNamer> getFileNamers() {
		return new ArrayList<AbstractFileNamer>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<TypePrinter> getTypePrinters() {
		return new ArrayList<TypePrinter>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<BatchFilter> getBatchFilters() {
		return new ArrayList<BatchFilter>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<XenaView> getViews() {
		return new ArrayList<XenaView>();
	}

	/**
	 * Default method which returns an empty list
	 */
	public List<PluginProperties> getPluginPropertiesList() {
		return new ArrayList<PluginProperties>();
	}

	/**
	 * Returns plugin version
	 */
	public abstract String getVersion();

	/**
	 * Returns plugin name
	 */
	public abstract String getName();

	public int compareTo(XenaPlugin comparePlugin) {
		return getName().compareTo(comparePlugin.getName());
	}

}
