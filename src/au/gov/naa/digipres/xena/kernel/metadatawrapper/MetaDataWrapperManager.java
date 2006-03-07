package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Manager for XMLFilter instances. These are primarily used to wrap normalised
 * objects with meta-data. We also have unwrap filters which can strip off the
 * meta-data. These wrappers and unwrappers form pairs.
 * 
 * @see org.xml.sax.XMLFilter
 * @author Chris Bitmead
 */
public class MetaDataWrapperManager implements LoadManager {

    static MetaDataWrapperManager theSingleton = new MetaDataWrapperManager();

    public static MetaDataWrapperManager singleton() {
        return theSingleton;
    }

    public final static String META_DATA_WRAPPER_PREF_NAME = "MetaDataWrapper";
    
    private final static String WRAP_NORMALISER_PREF = "wrapNormaliser";

    private final static String UNWRAP_NORMALISER_PREF = "unwrapNormaliser";

    private final static String DEFAULT_WRAPPER_NAME = "Default Meta Data wrapper";
    
    private MetaDataWrapperPlugin defaultMetaDataWrapper;
    
    private List<MetaDataWrapperPlugin> metaDataWrapperPlugins;

    private MetaDataWrapperPlugin activeWrapperPlugin;

    public MetaDataWrapperManager() {
        defaultMetaDataWrapper = new MetaDataWrapperPlugin("Default Meta Data wrapper", 
                new DefaultWrapper(), 
                new DefaultUnwrapper(), 
                DefaultWrapper.OPENING_TAG);

        metaDataWrapperPlugins = new ArrayList<MetaDataWrapperPlugin>();
        metaDataWrapperPlugins.add(defaultMetaDataWrapper);
        
        activeWrapperPlugin = defaultMetaDataWrapper;
    }

    private String basePathName = null;

    public MetaDataWrapperPlugin getDefaultWrapperPlugin(){
        return defaultMetaDataWrapper;
    }
    
    public boolean load(JarPreferences preferences) throws XenaException {
        JarPreferences root = (JarPreferences) JarPreferences.userNodeForPackage(MetaDataWrapperManager.class);
        String wrapperStr = root.get(WRAP_NORMALISER_PREF, null);
        String unwrapperStr = root.get(UNWRAP_NORMALISER_PREF, null);
        try {
            StringTokenizer spaceTokenizer = new StringTokenizer(preferences.get("filters", ""));
            while (spaceTokenizer.hasMoreTokens()) {
                // we have a new filter string - name/wrapper/unwrapper.
                // Create the tokenizer, and a new meta data wrapper plugin object.
                String filterString = spaceTokenizer.nextToken();
                StringTokenizer slashTokenizer = new StringTokenizer(filterString, "/");
                MetaDataWrapperPlugin metaDataWrapperPlugin = new MetaDataWrapperPlugin();
                
                // get the name
                if (slashTokenizer.hasMoreTokens()) {
                    metaDataWrapperPlugin.setName(slashTokenizer.nextToken());
                } else {
                    throw new XenaException("Bad Filter");
                }

                // get the wrapper class name and create a new instance of it.
                if (!slashTokenizer.hasMoreTokens()) {
                    throw new XenaException("Bad Filter");
                } 
                   
                Class wrapperClass = PluginManager.singleton().getClassLoader().loadClass(slashTokenizer.nextToken());
                try {
                    Object wrapperClassInstance = wrapperClass.newInstance();
                    if ( !(wrapperClassInstance instanceof XMLFilter)){
                        throw new XenaException("Bad Filter");
                    }
                    if ( !(wrapperClassInstance instanceof XenaWrapper)) {
                        throw new XenaException("Bad Filter");
                    }
                    metaDataWrapperPlugin.setWrapper((XMLFilter)wrapperClassInstance);
                    metaDataWrapperPlugin.setTopTag( ((XenaWrapper)wrapperClassInstance).getOpeningTag() );
                } catch (InstantiationException ie) {
                    throw new XenaException("Bad filter!");
                } catch (IllegalAccessException iae) {
                    throw new XenaException("Bad Filter");
                }
                
                
                // get the unwrapper class name and create a new instance of it.
                if (!slashTokenizer.hasMoreTokens()) {
                    throw new XenaException("Bad Filter");
                }
                
                Class unWrapperClass = PluginManager.singleton().getClassLoader().loadClass(slashTokenizer.nextToken());
                try {
                    Object unWrapperClassInstance = unWrapperClass.newInstance();
                    if ( !(unWrapperClassInstance instanceof XMLFilter)){
                        throw new XenaException("Bad Filter");
                    }
                    metaDataWrapperPlugin.setUnwrapper((XMLFilter)unWrapperClassInstance);
                } catch (InstantiationException ie) {
                    throw new XenaException("Bad Filter!");
                } catch (IllegalAccessException iae) {
                    throw new XenaException("Bad Filter!");
                }
                
                
                metaDataWrapperPlugins.add(metaDataWrapperPlugin);

                // notout filter name...
                // System.out.println("FILTER:" + filter.toString());

                // presumably, when we load a new meta data wrapper plugin, we want to override the default one. So, lets do that :)
                if (activeWrapperPlugin.getName().equals(DEFAULT_WRAPPER_NAME)) {
                    activeWrapperPlugin = metaDataWrapperPlugin;
                }
                
            }
            return !metaDataWrapperPlugins.isEmpty();
        } catch (ClassNotFoundException e) {
            throw new XenaException(e);
        }

    }

    public void complete() {
    }
    
    public List<MetaDataWrapperPlugin> getMetaDataWrapperPlugins(){ 
        return metaDataWrapperPlugins;
    }
    
    public List<String> getMetaDataWrapperNames() {
        List<String> rtnList = new ArrayList<String>();
        
        for ( MetaDataWrapperPlugin mdwp : metaDataWrapperPlugins) {
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
    }
    
    public MetaDataWrapperPlugin getActiveWrapperPlugin() {
        return activeWrapperPlugin;
    }
    
    
    public XMLFilter getWrapNormaliser() throws XenaException {
        return activeWrapperPlugin.getWrapper();
    }
    
    public XMLFilter getUnwrapNormaliser() throws XenaException {
        return activeWrapperPlugin.getUnwrapper();
    }

    public MetaDataWrapperPlugin getMetaDataWrapperByTag(String tag) throws XenaException {
        for (Iterator iter = metaDataWrapperPlugins.iterator(); iter.hasNext();) {
            MetaDataWrapperPlugin element = (MetaDataWrapperPlugin) iter.next();
            if (element.getTopTag().equals(tag) ) {
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
        XMLFilter wrapper = getMetaDataWrapperByTag(outerTag).getWrapper();
        if (wrapper instanceof XenaWrapper) {
            XenaWrapper xenaWrapper = (XenaWrapper)wrapper;
            return xenaWrapper.getSourceName(xis);            
        }
        throw new XenaException("Could not get Source Name with XenaWrapper: " + wrapper.getClass().getName());
    }
    
    public String getSourceId(XenaInputSource xis) throws XenaException {
        String outerTag = getTag(xis);
        XMLFilter wrapper = getMetaDataWrapperByTag(outerTag).getWrapper();
        if (wrapper instanceof XenaWrapper) {
            XenaWrapper xenaWrapper = (XenaWrapper)wrapper;
            return xenaWrapper.getSourceId(xis);            
        }
        throw new XenaException("Could not get Source ID with XenaWrapper: " + wrapper.getClass().getName());
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
    private String getTag(XenaInputSource xis) throws XenaException {
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            XMLFilter filter = new XMLFilterImpl();
            filter.setParent(reader);
            filter.setContentHandler(new XMLFilterImpl() {
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {

                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    throw new FoundException(localName, qName);
                }
            });
            reader.setContentHandler((ContentHandler) filter);
            reader.parse(xis);
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
     * @author andrek24 created 21/10/2005 This class provides an exception to
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
    
    
    
    
    
    
//    public void setWrapperClass(Class cls) {
//        JarPreferences root = (JarPreferences) JarPreferences.userNodeForPackage(MetaDataWrapperManager.class);
//        if (cls == null) {
//            wrapperClass = null;
//            unwrapper = null;
//        } else {
//            Iterator it = metaDataWrapperPlugins.iterator();
//            while (it.hasNext()) {
//                MetaDataWrapperPlugin filter = (MetaDataWrapperPlugin) it.next();
//                if (filter.getWrapClass().equals(cls)) {
//                    wrapperClass = filter.getWrapClass();
//                    unwrapper = filter.getUnwrapClass();
//                    break;
//                }
//            }
//        }
//        if (wrapperClass == null) {
//            root.remove(WRAP_NORMALISER_PREF);
//        } else {
//            root.put(WRAP_NORMALISER_PREF, wrapperClass.getName());
//        }
//        if (unwrapper == null) {
//            root.remove(UNWRAP_NORMALISER_PREF);
//        } else {
//            root.put(UNWRAP_NORMALISER_PREF, unwrapper.getName());
//        }
//    }
//
//
//    
//    
//    
//    
//    public List<XMLFilterImpl> getAllFilters() throws XenaException {
//        List<XMLFilterImpl> rtn = new ArrayList<XMLFilterImpl>();
//        Iterator it = metaDataWrapperPlugins.iterator();
//        while (it.hasNext()) {
//            MetaDataWrapperPlugin filter = (MetaDataWrapperPlugin) it.next();
//            try {
//                rtn.add(filter.getWrapperInstance());
//            } catch (IllegalAccessException e) {
//                throw new XenaException(e);
//            } catch (InstantiationException e) {
//                throw new XenaException(e);
//            }
//        }
//        return rtn;
//    }
//
//    public Class getWrapperClass() {
//        return wrapperClass;
//    }
//
//    public MetaDataWrapperPlugin getActiveFilter() {
//        return currentFilter;
//    }

//    // TODO: the following methods must be changed to figure out which wrapper
//    // to get!
//    public String getSourceId(XenaInputSource xis) throws XenaException {
//        String rtn = null;
//        XenaWrapper xenaWrapper = null;
//        if (wrapperClass != null) {
//            try {
//                xenaWrapper = (XenaWrapper) wrapperClass.newInstance();
//            } catch (InstantiationException e) {
//                System.out.println("Couldnt create XenaWrapper class. D'Oh.");
//                return "UNKNOWN_ID";
//            } catch (IllegalAccessException e) {
//                System.out.println("Couldnt create XenaWrapper class. D'Oh.");
//                return "UNKNOWN_ID";
//            }
//        }
//        if (xenaWrapper != null) {
//            try {
//                rtn = xenaWrapper.getSourceId(xis);
//            } catch (XenaException e) {
//                System.out
//                        .println("Couldnt get System ID from that particular file.");
//                throw new XenaException("NO ID");
//            }
//        }
//        return rtn;
//    }
//
//    // TODO: the following method must be changed to figure out which wrapper to
//    // get!
//    public String getSourceName(XenaInputSource xis) throws XenaException {
//        String rtn = null;
//        XenaWrapper xenaWrapper = null;
//        if (wrapperClass != null) {
//            try {
//                xenaWrapper = (XenaWrapper) wrapperClass.newInstance();
//            } catch (InstantiationException e) {
//                System.out.println("Couldnt create XenaWrapper class. D'Oh.");
//                return "UNKNOWN_ID";
//            } catch (IllegalAccessException e) {
//                System.out.println("Couldnt create XenaWrapper class. D'Oh.");
//                return "UNKNOWN_ID";
//            }
//        }
//        if (xenaWrapper != null) {
//            try {
//                rtn = xenaWrapper.getSourceName(xis);
//            } catch (XenaException e) {
//                System.out.println("Couldnt get System ID from that particular file.");
//                throw new XenaException("NO ID");
//            }
//        }
//        return rtn;
//    }


//    /**
//     * @return Returns the currentWrapper.
//     */
//    public XMLFilterImpl getActiveWrapper() throws XenaException {
//        try {
//            return currentFilter.getWrapperInstance();
//        } catch (IllegalAccessException e) {
//            throw new XenaException(e);
//        } catch (InstantiationException e) {
//            throw new XenaException(e);
//        }
//    }

}
