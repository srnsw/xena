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

package au.gov.naa.digipres.xena.kernel.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.ClassName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * Class for managing views within Xena.
 * Those methods that return a usable view require you to pass a "level". This
 * is the level within the gui hierarchy that the view will be used at. For
 * simple views this will be 0. But where views are nested within views the
 * number will be increasing according to its nestedness. Knowing how
 * deep a view is nested can be useful for various functions.
 *
 * @created    2 July 2002
 */
public class ViewManager {
	private List<XenaView> allViews = new ArrayList<XenaView>();
	private PluginManager pluginManager;

	// Determines whether the "Export" button will be displayed on Xena View frames
	private boolean showExportButton = false;

	public ViewManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		// add the built in views for Xena.
		// These are, default meta wrapper view and binary view. :)
		XenaView binaryView = new BinaryView();
		XenaView defaultView = new DefaultXenaView();

		binaryView.setViewManager(this);
		defaultView.setViewManager(this);

		allViews.add(binaryView);
		allViews.add(defaultView);
	}

	public void addViews(List<XenaView> viewList) {
		for (XenaView view : viewList) {
			view.setViewManager(this);
			allViews.add(view);
		}
	}

	/**
	 * Return a view for a particular XML type. Possibly consult the user.
	 * @param topXmlTag The outermost XML tag.
	 * @param viewType The type of view we need, whether regular or thumbnail.
	 * @param level Depth within gui hierarchy.
	 * @throws XenaException
	 */
	public XenaView getDefaultView(String topXmlTag, int viewType, int level) throws XenaException {
		return getDefaultViewNoAsk(topXmlTag, viewType, level);
	}

	/**
	 * Return a view for a given xena input source.
	 * Get the top level tag, and find the corresponding view type for that tag.
	 * @param XenaInputSource xis
	 * @throws XenaException
	 */
	public XenaView getDefaultView(XenaInputSource xis) throws XenaException {
		String topXMLTag = getTag(xis.getSystemId());

		// so now we get the view based on the tag.
		List<XenaView> views = lookup(topXMLTag, 0);

		if (views.size() <= 0) {
			throw new XenaException("No valid plugin or view to show type: " + topXMLTag);
		}
		XenaView view = views.get(0);

		// JRW - need to clone tag before use

		return cloneView(view, 0, topXMLTag);
	}

	/**
	 * Return the default view for a particular XML type. Don't consult the user.
	 * @param topXmlTag The outermost XML tag.
	 * @param viewType The type of view we need, whether regular or thumbnail.
	 * @param level Depth within gui hierarchy.
	 * @throws XenaException
	 */
	public XenaView getDefaultViewNoAsk(String topXmlTag, int viewType, int level) throws XenaException {
		List<XenaView> views = lookup(topXmlTag, level);

		if (views.size() <= 0) {
			// Last resort force
			views = lookup(topXmlTag, 0);
		}

		if (views.size() <= 0) {
			throw new XenaException("No valid plugin or view to show type: " + topXmlTag);
		}

		// Get the first view as a fallback
		XenaView chosenView = views.get(0);
		for (XenaView loopView : views) {
			if (loopView.getViewType() == viewType) {
				chosenView = loopView;
				break;
			}
		}

		return cloneView(chosenView, level, topXmlTag);
	}

	/**
	 * Return the plugin names of those plugins with views that can display
	 * a particular XML type. A good approximation of mapping XML types to
	 * plugins.
	 * @param topXmlTag XML tag.
	 * @return plugin names.
	 */
	public Set<String> getPluginNames(String topXmlTag) throws XenaException {
		Set<String> rtn = new HashSet<String>();
		List<XenaView> views = lookup(topXmlTag, -1);
		for (XenaView view : views) {
			String className = ClassName.packageComponent(view.getClass().getName());
			className.replace('.', '/');
			rtn.add(className);
		}
		return rtn;
	}

	public XenaView lookup(Class<?> cls, int level, String topXmlTag) {
		for (XenaView view : allViews) {
			if (view.getClass() == cls) {
				return cloneView(view, level, topXmlTag);
			}
		}
		return null;
	}

	/**
	 * Determines a list of XenaFileTypes based on the given XML tag.
	 * Don't use the returned view object for a real view!!! The views
	 * returned should be considered templates only. They need to be
	 * cloned, before being used. Pass the value to lookup(Class, int level)
	 * before you use! If you don't do this, very weird stuff is likely to
	 * happen.
	 *
	 * @param  xmlTag  XML tag of top level name.
	 * @return list of XenaViews associated with given XML tag
	 */
	public List<XenaView> lookup(String xmlTag, int level) throws XenaException {
		List<XenaView> viewList = new ArrayList<XenaView>();
		for (XenaView view : allViews) {
			if (view.canShowTag(xmlTag)) {
				viewList.add(view);
			}
		}

		// Sort list in order of priority
		Collections.sort(viewList, new PriorityComparator());
		return viewList;
	}

	/**
	 * Create a new XenaView of the same type as the given view.
	 * @param view XenaView to copy
	 * @param level level in the view hierarchy
	 * @param topXmlTag String
	 * @return XenaView new XenaView
	 */
	protected XenaView cloneView(XenaView view, int level, String topXmlTag) {
		try {
			XenaView rtn = view.getClass().newInstance();
			rtn.setViewManager(this);
			rtn.setTopTag(topXmlTag);
			rtn.setLevel(level);
			return rtn;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean changeView(XenaView oldView, XenaView newView) throws XenaException {
		if (newView != null && !newView.getClass().equals(oldView.getClass())) {
			XenaView pview = oldView.getParentView();
			JComponent comp = (JComponent) oldView.getParent();
			try {
				newView.setTmpFile(oldView.getTmpFile());
				newView.rewind();
			} catch (Exception x) {
				throw new XenaException(x);
			}
			pview.setSubView(comp, newView);
			newView.initListenersAndSubViews();
			return true;
		}
		return false;
	}

	/**
	 * @return Returns the allViews.
	 */
	public List<XenaView> getAllViews() {
		return allViews;
	}

	/**
	 * Get the outermost XML tag from a Xena document
	 * TODO: aak Is there possibly a better way of doing this than by throwing an exception when we find the tag?
	 * Was thinking of that whole whole object oriented design principal that exceptions are for exceptional behaviour...
	 * @param systemid
	 *            URL of document
	 * @return String tag
	 * @throws XenaException
	 */
	public String getTag(String systemid) throws XenaException {
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
			InputSource is = new InputSource(systemid);
			reader.setContentHandler((ContentHandler) filter);
			reader.parse(is);
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
		} catch (Exception x) {
			throw new XenaException(x);
		}
		throw new XenaException("getTag: Unknown Error");
	}

	/** 
	 * 
	 * created 21/10/2005
	 * This class provides an exception to allow us to exit parsing of an XML document quickly.
	 * It is used in the function getTag when trying to get the outermost tag of 
	 * (and thus identify the type of) a xena file.
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

	private class PriorityComparator implements Comparator<XenaView> {
		public int compare(XenaView view1, XenaView view2) {
			// We want larger priorities to be at the front of the list
			int priority1 = view1.getPriority();
			int priority2 = view2.getPriority();

			return priority2 - priority1;
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

	/**
	 * @return the showExportButton
	 */
	public boolean isShowExportButton() {
		return showExportButton;
	}

	/**
	 * @param showExportButton the showExportButton to set
	 */
	public void setShowExportButton(boolean showExportButton) {
		this.showExportButton = showExportButton;
	}

}
