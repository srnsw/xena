package au.gov.naa.digipres.xena.kernel.view;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
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
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.FoundException;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Class for managing views within Xena.
 * Those methods that return a usable view require you to pass a "level". This
 * is the level within the gui hierarchy that the view will be used at. For
 * simple views this will be 0. But where views are nested within views the
 * number will be increasing according to its nestedness. Knowing how
 * deep a view is nested can be useful for various functions.
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class ViewManager implements LoadManager {
	private List<XenaView> allViews = new ArrayList<XenaView>();

    private List<Class> viewClasses = new ArrayList<Class>();
    
	public static final String PROMPT_USER_VIEW_DEPTH = "promptUserViewDepth";

	public static final String MAXIMISE_NEW_VIEW = "maximiseNewView";

    
    private PluginManager pluginManager;
    
//	static ViewManager theSingleton = new ViewManager();
//	
//	public static ViewManager singleton() {
//	    return theSingleton;
//	}

    
    
    
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
        
        viewClasses.add(BinaryView.class);
        viewClasses.add(DefaultXenaView.class);
        
    }


    /**
     * Return a view for a particular XML type. Possibly consult the user.
     * @param topXmlTag The outermost XML tag.
     * @param viewType The type of view we need, whether regular or thumbnail.
     * @param level Depth within gui hierarchy.
     * @throws XenaException
     */
    public XenaView getDefaultView(String topXmlTag, int viewType, int level) throws XenaException {
        JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(ViewManager.class);
        int maxlevel = prefs.getInt(PROMPT_USER_VIEW_DEPTH, 0);
        if (maxlevel <= level) {
            return getDefaultViewNoAsk(topXmlTag, viewType, level);
        } else {
            // TODO: need to alter askView to use viewType
            return this.askView("Level: " + level + " Select View", topXmlTag, level);
        }
    }

    /**
     * Return a view for a given xena input source.
     * Get the top level tag, and find the corresponding view type for that tag.
     * @param XenaInputSource xis
     * @throws XenaException
     */
    public XenaView getDefaultView(XenaInputSource xis) throws XenaException {
        //sysout - checking sysid from xis....
        System.out.println(xis.getSystemId());
        
        String topXMLTag = getTag(xis.getSystemId());
        
        //sysout - put out top xml tag for this view...
        System.out.println("Tag:" + topXMLTag);
        
        //so now we get the view based on the tag.
        List views = lookup(topXMLTag, 0);
        
        if (views.size() <= 0) {
            throw new XenaException("No valid plugin or view to show type: " + topXMLTag);
        }
        XenaView view = (XenaView)views.get(0);
        
        System.out.println("views:"  + views.toString());
        
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
		java.util.List views = lookup(topXmlTag, level);
		if (views.size() <= 0) {
			// Last resort force
			views = lookup(topXmlTag, 0);
		}
		if (views.size() <= 0) {
			throw new XenaException("No valid plugin or view to show type: " + topXmlTag);
		} else {
			// Get the first view as a fallback
			XenaView view = (XenaView)views.get(0);
			Iterator it = views.iterator();
			// Then search for the first view of the appropriate type.
			while (it.hasNext()) {
				XenaView view2 = (XenaView)it.next();
				if (view2.getViewType() == viewType) {
					view = view2;
					break;
				}
			}
			return cloneView(view, level, topXmlTag);
		}
	}

	/**
	 * Return the plugin names of those plugins with views that can display
	 * a particular XML type. A good approximation of mapping XML types to
	 * plugins.
	 * @param topXmlTag XML tag.
	 * @return plugin names.
	 */
	public Set getPluginNames(String topXmlTag) throws XenaException {
		Set rtn = new HashSet();
		List views = lookup(topXmlTag, -1);
		Iterator it = views.iterator();
		while (it.hasNext()) {
			XenaView view = (XenaView)it.next();
			rtn.add(ClassName.classToPath(ClassName.packageComponent(view.getClass().getName())));
		}
		return rtn;
	}

    /**
     * load the classes and add them to the lisa viewClasses.
     * also instantiate each view and add it to the views class.
     * 
     * As this code loads classes from the classpath specified
     * in the preference file, it is possible that these classes
     * are not valid xena views. So we code defensively!
     * 
     */
	public boolean load(JarPreferences props) throws XenaException {
		try {
            
			PluginLoader loader = new PluginLoader(props);
			List views = loader.loadInstances("views");        
			for (Iterator it = views.iterator(); it.hasNext();) {
				XenaView view = (XenaView)it.next();
                view.setViewManager(this);
				addView(view);
			}
            viewClasses = loader.loadClasses("views");
            return !views.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}

	}

	/**
	 * Given an xml tag, ask the user what view to use to display it.
	 * @param name informational string
	 * @param topXmlTag xml tag
	 * @param level nestedness of this view
	 * @return XenaView
	 * @throws XenaException
	 */
	public XenaView askView(String name, String topXmlTag, int level) throws XenaException {
		java.util.List views = lookup(topXmlTag, level);
		XenaView view = askView(name, views, topXmlTag, level);
		if (view == null) {
			return null;
		} else {
			return cloneView(view, level, topXmlTag);
		}
	}

	public XenaView lookup(Class cls, int level, String topXmlTag) {
		Iterator it = allViews.iterator();
		while (it.hasNext()) {
			XenaView v = (XenaView)it.next();
			if (v.getClass() == cls) {
				return cloneView(v, level, topXmlTag);
			}
		}
		return null;
	}

	/**
	 * Determines a list of XenaFileTypes based on the given XML tag.
	 * Don't use the returned view object for a real view!!! The views
	 * returned should be considered templates only. They need to be
	 * cloned, before being used. Pass the value to lookup(Class, int level)
	 * before you use! If you don't do this, very wierd stuff is likely to
	 * happen.
	 *
	 * @param  xmlTag  XML tag of top level name.
	 * @return list of XenaViews associated with given XML tag
	 */

	public List<XenaView> lookup(String xmlTag, int level) throws XenaException {
		List<XenaView> rtn = new ArrayList<XenaView>();
		Iterator it = allViews.iterator();
		while (it.hasNext()) {
			XenaView v = (XenaView)it.next();
			if (v.canShowTag(xmlTag)) {
				rtn.add(v);
			}
		}
		return rtn;
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
			XenaView rtn = (XenaView)view.getClass().newInstance();
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

	/**
	 * Given an xml tag, ask the user what view to use to display it.
	 * @param name informational string
	 * @param views lits of possible views
	 * @param topXmlTag xml tag
	 * @param level nestedness of this view
	 * @return XenaView
	 * @throws XenaException
	 */
	protected XenaView askView(String name, java.util.List views, String topXmlTag, int level) {
		XenaView view = null;
		if (name == null) {
			name = "";
		} else {
			name += ": ";
		}
		XenaView vi = (XenaView)JOptionPane.showInputDialog(null, name + "Choose View", name + "Choose View", JOptionPane.QUESTION_MESSAGE, null,
															views.toArray(), null);
		if (vi != null) {
			view = cloneView(vi, level, topXmlTag);
		}
		return view;
	}

	/**
	 *  Adds an XenaView to the view list
	 *
	 * @param  view  The XenaView to be added to the view list
	 */
	protected void addView(XenaView view) {
		allViews.add(view);
	}

	public void complete() {
	}

	public boolean changeView(XenaView oldView, XenaView newView) throws XenaException {
		if (!newView.getClass().equals(oldView.getClass()) && newView != null) {
			XenaView pview = oldView.getParentView();
			JComponent comp = (JComponent)oldView.getParent();
			try {
				newView.setTmpFile(oldView.getTmpFile());
				newView.setInternalFrame(oldView.getInternalFrame());
				newView.rewind();
			} catch (Exception x) {
				throw new XenaException(x);
			}
			pview.setSubView(comp, newView);
			pview.initSubViews();
			newView.initListenersAndSubViews();
			pview.getInternalFrame().makeMenu();
			pview.getInternalFrame().setDefaultSize();
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
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {

                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    throw new FoundException(localName, qName);
                }
            });
            InputSource is = new InputSource(systemid);
            reader.setContentHandler((ContentHandler) filter);
            reader.parse(is);
        } catch (FoundException e) {
            if (e.qtag == null || e.qtag.equals("")) {
                return e.tag;
            } else {
                return e.qtag;
            }
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
     * @author andrek24
     * created 21/10/2005
     * This class provides an exception to allow us to exit parsing of an XML document quickly.
     * It is used in the function getTag when trying to get the outermost tag of 
     * (and thus identify the type of) a xena file.
     */
    private class FoundException extends SAXException {
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
