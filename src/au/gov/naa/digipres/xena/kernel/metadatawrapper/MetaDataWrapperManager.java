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

package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * Manager for XMLFilter instances. These are primarily used to wrap normalised
 * objects with meta-data. We also have unwrap filters which can strip off the
 * meta-data. These wrappers and unwrappers form pairs.
 * 
 * @see org.xml.sax.XMLFilter
 */
public class MetaDataWrapperManager {

	public final static String META_DATA_WRAPPER_PREF_NAME = "MetaDataWrapper";

	// built in wrappers...
	public final static String DEFAULT_WRAPPER_NAME = "Default Meta Data wrapper";
	public final static String EMPTY_WRAPPER_NAME = "Empty Meta Data Wrapper";

	private MetaDataWrapperPlugin defaultMetaDataWrapper;
	private MetaDataWrapperPlugin emptyWrapper;

	private List<MetaDataWrapperPlugin> metaDataWrapperPlugins;

	private MetaDataWrapperPlugin activeWrapperPlugin;

	private PluginManager pluginManager;

	/**
	 * Indicates that a metadata wrapper has not been automatically loaded from a plugin,
	 * or set using setActiveMetaDataWrapper. This removes a potential problem where
	 * the user manually selects the DefaultMetaDataWrapper, but then loads a new plugin
	 * with a MetaDataWrapper which would overwrite the DefaultMetaDataWrapper.
	 */
	private boolean activeMetaDataWrapperUnchanged = true;

	public MetaDataWrapperManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;

		defaultMetaDataWrapper =
		    new MetaDataWrapperPlugin(DEFAULT_WRAPPER_NAME, new DefaultWrapper(), new DefaultUnwrapper(), DefaultWrapper.OPENING_TAG, this);

		emptyWrapper = new MetaDataWrapperPlugin(EMPTY_WRAPPER_NAME, new EmptyWrapper(), new EmptyUnwrapper(), "", this);

		metaDataWrapperPlugins = new ArrayList<MetaDataWrapperPlugin>();
		metaDataWrapperPlugins.add(defaultMetaDataWrapper);
		metaDataWrapperPlugins.add(emptyWrapper);

		activeWrapperPlugin = defaultMetaDataWrapper;
	}

	private String basePathName = null;

	public MetaDataWrapperPlugin getDefaultWrapper() {
		return defaultMetaDataWrapper;
	}

	public MetaDataWrapperPlugin getEmptyWrapper() {
		return emptyWrapper;
	}

	/**
	 * Return the meta data wrapper plugin corresponding to the name provided.
	 * If no plugin with the name exists, return null.
	 * 
	 * @param name of the plugin
	 * @return The metaDataWrapper plugin corresponding to the name; null if none exists.
	 */
	public MetaDataWrapperPlugin getMetaDataWrapperPluginByName(String name) {
		for (MetaDataWrapperPlugin element : metaDataWrapperPlugins) {
			if (element.getName().equals(name)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Add the wrapper and unwrapper pairs to the Meta Data Wrapper Manager.
	 * Given that the first wrapper loaded will become the default wrapper, iteration order could be important.
	 * In this case, use a LinkedHashMap to ensure entries are iterated in the order they were put into the map.
	 * @param wrapperMap
	 */
	public void addMetaDataWrappers(Map<AbstractMetaDataWrapper, XMLFilter> wrapperMap) {

		for (AbstractMetaDataWrapper wrapper : wrapperMap.keySet()) {
			MetaDataWrapperPlugin metaDataWrapperPlugin = new MetaDataWrapperPlugin();
			metaDataWrapperPlugin.setMetaDataWrapperManager(this);

			metaDataWrapperPlugin.setWrapper(wrapper.getClass());
			metaDataWrapperPlugin.setTopTag(wrapper.getOpeningTag());
			metaDataWrapperPlugin.setName(wrapper.getName());

			metaDataWrapperPlugin.setUnwrapper(wrapperMap.get(wrapper));

			metaDataWrapperPlugins.add(metaDataWrapperPlugin);

			// When we first load a new, non-default meta data wrapper plugin, we want to override the default one. 
			if (activeMetaDataWrapperUnchanged) {
				activeWrapperPlugin = metaDataWrapperPlugin;
				activeMetaDataWrapperUnchanged = false;
			}
		}
	}

	public List<MetaDataWrapperPlugin> getMetaDataWrapperPlugins() {
		return metaDataWrapperPlugins;
	}

	public List<String> getMetaDataWrapperNames() {
		List<String> rtnList = new ArrayList<String>();

		for (MetaDataWrapperPlugin mdwp : metaDataWrapperPlugins) {
			rtnList.add(mdwp.getName());
		}
		return rtnList;
	}

	/**
	 * @param activeWrapperPlugin The new value to set activeWrapperPlugin to.
	 * This should be taken from the wrapper plugin list.
	 */
	public void setActiveWrapperPlugin(MetaDataWrapperPlugin activeWrapperPlugin) {
		if (activeWrapperPlugin == null) {
			throw new IllegalArgumentException();
		}
		this.activeWrapperPlugin = activeWrapperPlugin;
		activeMetaDataWrapperUnchanged = false;
	}

	public MetaDataWrapperPlugin getActiveWrapperPlugin() {
		return activeWrapperPlugin;
	}

	public AbstractMetaDataWrapper getWrapNormaliser() throws XenaException {
		AbstractMetaDataWrapper metaDataWrapper = activeWrapperPlugin.getWrapper();
		metaDataWrapper.setMetaDataWrapperManager(this);
		return metaDataWrapper;
	}

	public XMLFilter getUnwrapNormaliser() throws XenaException {
		return activeWrapperPlugin.getUnwrapper();
	}

	private MetaDataWrapperPlugin getMetaDataWrapperByTag(String tag) throws XenaException {
		for (MetaDataWrapperPlugin element : metaDataWrapperPlugins) {
			if (element.getTopTag().equals(tag)) {
				return element;
			}
		}
		throw new XenaException("No Meta Data Wrapper for that tag!");
	}

	/**
	 * @return Returns the basePathName.
	 */
	public synchronized String getBasePathName() {
		return basePathName;
	}

	/**
	 * @param basePathName
	 *            The new value to set basePathName to.
	 */
	public synchronized void setBasePathName(String basePathName) {
		this.basePathName = basePathName;
	}

	/**
	 * 
	 * @param xis
	 * @return the appropriate unwrapper.
	 * @throws XenaException
	 */
	public XMLFilter getUnwrapper(XenaInputSource xis) throws XenaException {
		String outerTag = getTag(xis);
		return getMetaDataWrapperByTag(outerTag).getUnwrapper();
	}

	public String getSourceName(XenaInputSource xis) throws XenaException {
		String outerTag = getTag(xis);
		AbstractMetaDataWrapper wrapper = getMetaDataWrapperByTag(outerTag).getWrapper();
		wrapper.setMetaDataWrapperManager(this);
		return wrapper.getSourceName(xis);
	}

	public String getSourceId(XenaInputSource xis) throws XenaException {
		String outerTag = getTag(xis);
		AbstractMetaDataWrapper wrapper = getMetaDataWrapperByTag(outerTag).getWrapper();
		wrapper.setMetaDataWrapperManager(this);
		return wrapper.getSourceId(xis);
	}

	/**
	 * Get the outermost XML tag from a Xena document 
	 * TODO: aak Is there possibly a better way of doing this than by throwing 
	 * an exception when we find the tag? Was thinking of that whole whole object 
	 * oriented design principal that exceptions are for exceptional behaviour...
	 * 
	 * TODO: THIS SHOULD BE MOVED TO A UTILITY METHOD THAT CAN BE ACCESED BY ANY XENA CLASS...
	 * 
	 * @param systemid
	 *            URL of document
	 * @return String tag
	 * @throws XenaException
	 */
	public String getTag(XenaInputSource xis) throws XenaException {
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			XMLFilter filter = new XMLFilterImpl();
			filter.setParent(reader);
			filter.setContentHandler(new XMLFilterImpl() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					// Bail out early as soon as we've found what we want
					// for super efficiency.
					throw new FoundException(localName, qName);
				}
			});
			reader.setContentHandler((ContentHandler) filter);
			reader.parse(xis);
		} catch (FoundException e) {
			if (e.qtag == null || "".equals(e.qtag)) {
				return e.tag;
			}
			return e.qtag;

		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		}
		throw new XenaException("getTag: Unknown Error");
	}

	/**
	 * 
	 *         allow us to exit parsing of an XML document quickly. It is used
	 *         in the function getTag when trying to get the outermost tag of
	 *         (and thus identify the type of) a xena file.
	 */
	private class FoundException extends SAXException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private String tag;

		private String qtag;

		public FoundException(String tag, String qtag) {
			super("Found");
			this.tag = tag;
			this.qtag = qtag;
		}

		public String getQtag() {
			return qtag;
		}

		public String getTag() {
			return tag;
		}

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
