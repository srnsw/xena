package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.Reflect;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.plugin.LoadManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.BinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.XenaBinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * This class is responsible for managing all things pertaining to normalisers.
 * <p>
 * It basically does the following broad tasks:
 * <ul><li>Loads Normalisers</li>
 * <li>Loads De-Normalisers</li>
 * <li>Maintains the relationship of types to normalisers / denormalisers</li>
 * <li>Handles the actual normalisation process</li>
 * <li>Handles exporting (Denormalisation)</li>
 * </ul>
 * <p>
 * The binary normaliser and denormaliser are built into this class. Other
 * normalisers are able to be loaded as part of plugins using the loadManager
 * interface methods (<code>load(JarPreferences jp)</code> and <code>complete()</code>).
 * This is usually called by the plugin manager. Otherwise, this class allows 
 * normalisers and denormalisers to be retrieved based on their name, class or
 * class name, tags, input type or output type. At this stage, there are a number
 * of Maps which are maintained to facilitate this, however, ultimately this may have to
 * be changed. These changes will be transparent to calling applications. 
 * This class also allows normalisers to be 'disabled', although this feature
 * is deprecated and types should be disabled rather than normalisers.
 * </p><p>
 * Finally, the normaliser manager is responsible for the actual normalising of
 * XenaInputSource objects, including wrapping, as well as the Denormalising
 * of Xena files to the appropriate format they are required to be exported to.
 * </p>
 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser
 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser
 * @see au.gov.naa.digipres.xena.kernel.plugin.PluginManager
 * @author Andrew Keeling
 * @author Justin Waddell
 * @author Chris Bitmead
 * @created April 8, 2002
 */
public class NormaliserManager implements LoadManager {

    public final static String PREF_AUTO_LOG = "autoLog";
    public final static String SOURCE_DIR_STRING = "sourceDirectory";
    public final static String DESTINATION_DIR_STRING = "destinationDirectory";
    public final static String ERROR_DIR_STRING = "errorDirectory";
    public final static String CONFIG_DIR_STRING = "configDirectory";
    public final static String DEACTIVATED_INPUT_TYPES_STRING = "deactivatedInputTypes";

    private Map<Object, List<Class>> fromMap = new HashMap<Object, List<Class>>();

    private Map<String, Class> nameMap = new HashMap<String, Class>();

    private Set<Class> all = new HashSet<Class>();

    private Map<String, List<Class>> denormaliserTagMap = new HashMap<String, List<Class>>();

    private Map<Class, Set<Type>> outputTypes = new HashMap<Class, Set<Type>>();

    private Map<Class, Set<Type>> inputTypes = new HashMap<Class, Set<Type>>();

    private Map<Class, Set<Type>> deactivatedInputTypes = new HashMap<Class, Set<Type>>();

    private Map<String, AbstractNormaliser> normaliserMap = new HashMap<String, AbstractNormaliser>();

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private PluginManager pluginManager;

    /**
     * Constructor for the NormaliserManager class.
     * @param pluginManager
     */
    public NormaliserManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;

        // add our builtin normalisers with the built in input types.
        Set<Type> binaryTypes = new HashSet<Type>();
        binaryTypes.add(new BinaryFileType());

        Set<Type> xenaBinaryTypes = new HashSet<Type>();
        xenaBinaryTypes.add(new XenaBinaryFileType());

        AbstractNormaliser binaryNormaliser = new BinaryToXenaBinaryNormaliser();
        binaryNormaliser.setNormaliserManager(this);
        normaliserMap.put(binaryNormaliser.toString(),
                (AbstractNormaliser) binaryNormaliser);

        inputTypes.put(BinaryToXenaBinaryNormaliser.class, binaryTypes);
        inputTypes.put(XenaBinaryToBinaryDeNormaliser.class, xenaBinaryTypes);

        outputTypes.put(BinaryToXenaBinaryNormaliser.class, xenaBinaryTypes);
        outputTypes.put(XenaBinaryToBinaryDeNormaliser.class, binaryTypes);

        try {
            add(BinaryToXenaBinaryNormaliser.class, (Collection) inputTypes
                    .get(BinaryToXenaBinaryNormaliser.class),
                    (Collection) outputTypes
                            .get(BinaryToXenaBinaryNormaliser.class));
        } catch (XenaException xe) {
            System.err.println("Could not load binary normaliser.");
            xe.printStackTrace();
        }
        try {
            add(XenaBinaryToBinaryDeNormaliser.class, (Collection) inputTypes
                    .get(XenaBinaryToBinaryDeNormaliser.class),
                    (Collection) outputTypes
                            .get(XenaBinaryToBinaryDeNormaliser.class));
        } catch (XenaException xe) {
            System.err.println("Could not load binary de-normaliser.");
            xe.printStackTrace();
        }
    }

    public boolean load(JarPreferences prefs) throws XenaException {
        Map<Class, Set<Type>> input = getTypes(prefs, "inputMap");
        inputTypes.putAll(input);
        Map<Class, Set<Type>> output = getTypes(prefs, "outputMap");
        outputTypes.putAll(output);
        Iterator it = input.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Class cls = (Class) entry.getKey();
            add(cls, (Collection) entry.getValue(), (Collection) output.get(cls));
        }
        return !output.isEmpty();
    }

    /**
     * Get a list of all available normalisers, denormalisers and other objects
     * 
     * @return List
     */
    public List getAll() {
        // TODO: what types should these be?
        List<Object> rtn = new ArrayList<Object>();
        Iterator it = all.iterator();
        while (it.hasNext()) {
            Class cls = (Class) it.next();
            try {
                rtn.add(cls.newInstance());
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            }
        }
        return rtn;
    }

    /**
     * Get a list of all available normalisers (i.e. XMLReader objects)
     * 
     * @return List
     */
    public List<AbstractNormaliser> getAllNormalisers() {
        List<AbstractNormaliser> rtn = new ArrayList<AbstractNormaliser>();
        Iterator it = all.iterator();
        while (it.hasNext()) {
            Class cls = (Class) it.next();
            if (Reflect.conformsTo(cls, AbstractNormaliser.class)) {
                try {
                    rtn.add((AbstractNormaliser) cls.newInstance());
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return rtn;
    }
    
    public String toString() {
        Iterator it = fromMap.entrySet().iterator();
        String rtn = "Normalisers: \n";
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            rtn += e.getKey().toString() + " -> " + e.getValue() + "\n";
        }
        return rtn;
    }

    /**
     * Return all the normalisers classes that have the input type given
     * 
     * @param from
     *            Type
     * @return List
     */
    public List<Class> lookupList(Type from) {
        return fromMap.get(from);
    }

    /**
     * Return return an instance of a normaliser given the input type
     * 
     * @param type
     *            Type
     * @return List
     */
    public AbstractNormaliser lookup(Type type) throws XenaException {
        AbstractNormaliser rtn = null;
        try {
            Class cls = lookupClass(type);
            if (cls != null) {
                rtn = (AbstractNormaliser) cls.newInstance();
                rtn.setNormaliserManager(this);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public AbstractNormaliser lookup(String name) {
        AbstractNormaliser rtn = null;
        rtn = normaliserMap.get(name);
        if (rtn != null) {
            rtn.setNormaliserManager(this);
        }
        return rtn;
    }

    /**
     * Return an instance of a normaliser given its class object
     * 
     * @param cls
     *            Class
     * @return Object
     */
    public Object lookupByClass(Class cls) {
        Object rtn = null;
        try {
            if (cls != null) {
                rtn = cls.newInstance();
                if (rtn instanceof AbstractNormaliser) {
                    ((AbstractNormaliser) rtn).setNormaliserManager(this);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    /**
     * Return an instance of a normaliser given its class name.
     * 
     * @param clsName
     *            String
     * @return Object
     */
    public Object lookupByClassName(String clsName) {
        Class cls = (Class) nameMap.get(clsName);
        Object rtn = lookupByClass(cls);
        return rtn;
    }

    /**
     * Load a given preference and parse the pattern:
     * NormaliserClass/Type[,Type] [NormaliserClass/Type[,Type]] ^stuff enclosed
     * within '[' and ']' may be repeated 0 or more times. Return a map: class ->
     * list of types.
     * 
     * @param prefs
     *            the preferences object
     * @param mapName
     *            the preference name key
     * @return Map<Class, List<Type>>
     * @throws XenaException
     */
    protected Map<Class, Set<Type>> getTypes(JarPreferences prefs,
            String mapName) throws XenaException {
        Map<Class, Set<Type>> typesMap = new HashMap<Class, Set<Type>>();
        String typeToNormaliserMap = prefs.get(mapName, "");
        StringTokenizer typeToNormaliserMapTokenizer = new StringTokenizer(
                typeToNormaliserMap);
        // cycle through each NormaliserClass / TypeList pair.
        while (typeToNormaliserMapTokenizer.hasMoreTokens()) {
            String typeToNormaliserString = typeToNormaliserMapTokenizer
                    .nextToken();
            StringTokenizer typeToNormaliserComponent = new StringTokenizer(
                    typeToNormaliserString, "/");
            if (!typeToNormaliserComponent.hasMoreTokens()) {
                throw new XenaException("Bad normaliserMap: "
                        + typeToNormaliserString);
            }
            String normName = typeToNormaliserComponent.nextToken();
            Set<Type> list = new HashSet<Type>();

            Class cls = null;
            try {
                cls = this.getClass().getClassLoader().loadClass(normName);
            } catch (ClassNotFoundException e) {
                // ignore exception, and try again.
            }

            if (cls == null) {
                try {
                    cls = pluginManager.getDeserClassLoader().loadClass(
                            normName);
                } catch (ClassNotFoundException e) {
                    // ignore... throw exception if cls is still null.
                }
            }
            if (cls == null) {
                throw new XenaException("Unable to load class: " + normName);
            }

            typesMap.put(cls, list);
            // load the class.
            try {
                Object classInstance = cls.newInstance();
                if (classInstance instanceof AbstractNormaliser) {
                    normaliserMap.put(classInstance.toString(),
                            (AbstractNormaliser) classInstance);
                } 
            } catch (Exception e) {
                // sysout - print exception if class unable to be instantiated
                // and added to the list.
                logger
                        .log(
                                Level.FINER,
                                "Class ["
                                        + normName
                                        + "] was not able to be added to the normaliser / denormaliser lists",
                                e);
                e.printStackTrace();
            }
            if (!typeToNormaliserComponent.hasMoreTokens()) {
                throw new XenaException("Bad normaliserMap: "
                        + typeToNormaliserString);
            }
            String types = typeToNormaliserComponent.nextToken();
            StringTokenizer st3 = new StringTokenizer(types, ",");
            while (st3.hasMoreTokens()) {
                String typeName = st3.nextToken();
                Type type = pluginManager.getTypeManager().lookupByClassName(
                        typeName);
                if (type == null) {
                    throw new XenaException("Bad normaliserMap, unknown type: "
                            + typeName + " ... " + typeToNormaliserString);
                }
                list.add(type);
            }
        }
        return typesMap;
    }

    /**
     * Add a normaliser class to the Xena installation
     * 
     * @param cls
     *            normaliser class (XMLReader).
     * @param input
     *            collection of input types allowable for this normaliser
     * @param output
     *            collection of output types possible for this normaliser
     * @throws XenaException
     */
    protected void add(Class cls, Collection input, Collection output)
            throws XenaException {
        try {
            Object norm = cls.newInstance();
            if (!(norm instanceof AbstractNormaliser)
                    && !(norm instanceof AbstractDeNormaliser)) {
                throw new XenaException(
                        "Error: this object does not appear to be a normaliser - "
                                + norm.getClass().getName());
            } else {
                if (norm instanceof AbstractNormaliser) {
                    ((AbstractNormaliser) norm).setNormaliserManager(this);
                } else if (norm instanceof AbstractDeNormaliser) {
                    ((AbstractDeNormaliser) norm).setNormaliserManager(this);
                }
            }

            if (output == null) {
                throw new XenaException("Error: outputMap for: "
                        + norm.getClass().getName() + "is null!");
            }
            if (output.size() != 1) {
                throw new XenaException("Error: outputMap for: "
                        + norm.getClass().getName() + " has: " + output.size()
                        + " but must have exactly one entry per normaliser");
            }

            if (input == null) {
                throw new XenaException("Error: inputMap for: "
                        + norm.getClass().getName() + "is null!");
            }
            if (input.size() == 0) {
                throw new XenaException("No input types for: "
                        + norm.getClass().getName());
            }
            Iterator setIt = input.iterator();
            while (setIt.hasNext()) {
                Type type = (Type) setIt.next();
                add(cls, type, norm);
                Iterator it = Reflect.allSuper(type.getClass()).iterator();
                while (it.hasNext()) {
                    add(cls, it.next(), norm);
                }
            }
        } catch (IllegalAccessException x) {
            throw new XenaException(x);
        } catch (InstantiationException x) {
            throw new XenaException(x);
        }
    }

    /**
     * Given a normaliser class, return the output type
     * 
     * @param cls
     *            normaliser Class
     * @return FileType
     */
    public FileType getOutputType(Class cls) {
        return (FileType) ((Collection) outputTypes.get(cls)).iterator().next();
    }

    /**
     * Given a normaliser class, return the possible input types
     * 
     * @param cls
     *            normaliser Class
     * @return FileType
     */
    @Deprecated
    public Set getInputTypes(Class cls) {
        return (Set) inputTypes.get(cls);
    }

    /**
     * Given a normaliser class, return the deactivated input types
     * 
     * @param cls
     *            normaliser Class
     * @return FileType
     */
    @Deprecated
    public Set<Type> getDeactivatedInputTypes(Class cls) {
        return deactivatedInputTypes.get(cls);
    }

    /**
     * Return the Map of deactivated input types
     * 
     * @return Map<Class, List<Type>>
     */
    @Deprecated
    public Map<Class, Set<Type>> getDeactivatedInputTypes() {
        return deactivatedInputTypes;
    }

    /**
     * Add the normaliser to the main configuration collections
     * 
     * @param cls
     *            XMLReader Class
     * @param normaliser
     *            XMLReader object
     */
    protected void addName(Class cls, Object normaliser) {
        all.add(cls);
        nameMap.put(cls.getName(), cls);
    }

    /**
     * Add the given normaliser to the configuration
     * 
     * @param cls
     *            class object of normaliser
     * @param type
     *            Object
     * @param normaliser
     *            Object
     */
    protected void add(Class cls, Object type, Object normaliser) {
        addName(cls, normaliser);
        List<Class> normaliserClassList = fromMap.get(type);
        if (normaliserClassList == null) {
            normaliserClassList = new ArrayList<Class>();
            fromMap.put(type, normaliserClassList);
        }
        normaliserClassList.add(cls);

        if (normaliser instanceof AbstractDeNormaliser && type instanceof XenaFileType) {
            XenaFileType xft = (XenaFileType) type;
            normaliserClassList = denormaliserTagMap.get(xft.getTag());
            if (normaliserClassList == null) {
                normaliserClassList = new ArrayList<Class>();
                denormaliserTagMap.put(xft.getTag(), normaliserClassList);
            }
            normaliserClassList.add(cls);
        }
    }

    /**
     * Find a DeNormaliser keyed on the XML tag that it can handle
     */
    public AbstractDeNormaliser lookupDeNormaliser(String tag) {
        try {
	        List l = (List) denormaliserTagMap.get(tag);
	        if (l == null) {
	            return null;
	        }
	        Class cls = (Class) l.get(0);
        	AbstractDeNormaliser denormaliser = (AbstractDeNormaliser) cls.newInstance();
        	denormaliser.setNormaliserManager(this);
            return denormaliser;
       } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a DeNormaliser keyed on the Xena type that it can handle
     */
    public AbstractDeNormaliser lookupDeNormaliser(Type type) {
        List list = lookupList(type);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Class cls = (Class) it.next();
            if (Reflect.conformsTo(cls, TransformerHandler.class)) {
                try {
                	AbstractDeNormaliser denormaliser = (AbstractDeNormaliser) cls.newInstance();
                	denormaliser.setNormaliserManager(this);
                    return denormaliser;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Lookup a normaliser based on the type needed.
     * 
     * @param type
     *            Type
     * @return Class
     * @throws XenaException
     */
    protected Class lookupClass(Type type) throws XenaException {
        Class rtn = null;
        List list = lookupList(type);
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Class cls = (Class) it.next();
                if (Reflect.conformsTo(cls, XMLReader.class)) {
                    Set<Type> types = deactivatedInputTypes.get(cls);
                    if (types == null || !types.contains(type)) {
                        rtn = cls;
                        break;
                    }
                }
            }
        }
        if (rtn == null) {
            throw new XenaException("No Normaliser available");
        }
        return rtn;
    }


    /**
     * An exception class to throw when we find a tag we were looking for in a
     * SAX parse. Throwing an exception allows us to abandon the parse as soon
     * as we found what we were looking for which is more efficient.
     */
    static private class FoundException extends SAXException {
        String tag;

        String qtag;

        public FoundException(String tag, String qtag) {
            super("Found");
            this.tag = tag;
            this.qtag = qtag;
        }
    }

    /**
     * Given a URL, unwrap the package wrapper and discover what the outermost
     * XML tag is for this document
     * 
     * @param systemid
     *            URL of Xena document
     * @return String XML tag of outermost XML
     */
    public String unwrapGetTag(XenaInputSource xis, XMLFilter unwrapper)
            throws XenaException {
        
        
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            unwrapper.setContentHandler(new XMLFilterImpl() {
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {

                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    throw new FoundException(localName, qName);
                }
            });
            reader.setContentHandler((ContentHandler) unwrapper);
            reader.parse(xis);

        } catch (FoundException e) {
            if (e.qtag == null || "".equals( e.qtag )) {
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
        } finally {
            try {
                xis.close();
            } catch (IOException iox) {
                throw new XenaException(iox);
            }
       }
        throw new XenaException("unwrapGetTag: Unknown Tag");
        
        
        
    }

    /**
     * Given a URL, unwrap the package wrapper and discover what the outermost
     * XML tag is for this document
     * 
     * @param systemid
     *            URL of Xena document
     * @return String XML tag of outermost XML
     */
    public String getFirstContentTag(String systemid) throws XenaException {
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            XMLFilter unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapNormaliser();
            unwrapper.setParent(reader);
            unwrapper.setContentHandler(new XMLFilterImpl() {

                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {

                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    throw new FoundException(localName, qName);
                }
            });
            InputSource is = new InputSource(systemid);
            reader.setContentHandler((ContentHandler) unwrapper);
            reader.parse(is);
        } catch (FoundException e) {
            if (e.qtag == null || "".equals( e.qtag )) {
                return e.tag;
            } else {
                return e.qtag;
            }
        } catch (SAXException x) {
            throw new XenaException(x);
        } catch (ParserConfigurationException x) {
            throw new XenaException(x);
        } catch (XenaException x) {
            throw new XenaException(x);
        } catch (IOException x) {
            throw new XenaException(x);
        } catch (Exception x) {
            throw new XenaException(x);
        }
        throw new XenaException("unwrapGetTag: Unknown Tag");
    }

    /**
     * Get the outermost XML tag from a Xena document TODO: Should this be in
     * here? i mean, this isnt really anything to with normalising is it?
     * 
     * @param systemid
     *            URL of document
     * @return String tag
     * @throws XenaException
     */
    public String getTag(String systemid) throws XenaException {
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
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
            InputSource is = new InputSource(systemid);
            reader.setContentHandler((ContentHandler) filter);
            reader.parse(is);
        } catch (FoundException e) {
            if (e.qtag == null || "".equals( e.qtag )) {
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
     * Parse a fragment of an XML document, stripping off the package wrapper
     * 
     * @param systemid
     *            URL of document
     * @param ch
     *            ContentHandler
     */
    public void unwrapFragment(String systemid, ContentHandler ch)
            throws ParserConfigurationException, SAXException, IOException,
            XenaException {
        XMLFilterImpl filter = new XMLFilterImpl() {
            public void startDocument() {
            }

            public void endDocument() {
            }
        };
        filter.setContentHandler(ch);
        unwrap(systemid, filter);
    }

    /**
     * Parse an XML document, stripping off the package wrapper
     * 
     * @param systemid
     *            URL of document
     * @param ch
     *            ContentHandler
     */
    public void unwrap(String systemid, ContentHandler ch)
            throws ParserConfigurationException, SAXException, IOException,
            XenaException {
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
                .getXMLReader();
        XMLFilter unwrapper = pluginManager.getMetaDataWrapperManager()
                .getUnwrapNormaliser();
        unwrapper.setParent(reader);
        unwrapper.setContentHandler(ch);
        InputSource is = new InputSource(systemid);
        reader.setContentHandler((ContentHandler) unwrapper);
        reader.parse(is);
    }


    /**
     * Return a XMLFilter with suitable chaining of events so that the
     * output file will be wrapped with appropriate meta-data.
     * This is an embedded normaliser, thus we need to ensure that
     * start and end document events are not passed on to the primary
     * content handler.
     * 
     * @param normaliser
     *            normaliser to be used
     * @param xis
     *            source of data
     * @param mesgLevel
     *            level of embedding within the output Xena file
     * @return XMLFilter which will handle the XML stream
     */
    public AbstractMetaDataWrapper wrapEmbeddedNormaliser(AbstractNormaliser normaliser, XenaInputSource xis, ContentHandler primaryHandler) 
    throws SAXException, XenaException 
    {
        AbstractMetaDataWrapper filter = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getEmbeddedWrapper();
        AbstractMetaDataWrapper embeddedWrapper = wrapTheNormaliser(normaliser, xis, filter);
        
        // Filter to ensure start and end document events are not passed on to the primaryHandler
        XMLFilterImpl embeddedFilter = new XMLFilterImpl() 
        {
            public void startDocument() {};

            public void endDocument() {};
        };
        embeddedWrapper.setContentHandler(embeddedFilter);
        embeddedFilter.setContentHandler(primaryHandler);
        embeddedFilter.setParent(embeddedWrapper);
        
        return embeddedWrapper;
    }

    /**
     * Return a XMLFilter with suitable chaining of events so that the
     * output file will be wrapped with appropriate meta-data
     * 
     * @param normaliser -
     *            normaliser to be used
     * @param xis -
     *            source of data
     * @param wrapper -
     *            xml wrapper
     * @return XMLFilter which will handle the XML stream
     */
    public AbstractMetaDataWrapper wrapTheNormaliser(AbstractNormaliser normaliser,
            XenaInputSource xis, AbstractMetaDataWrapper wrapper) throws SAXException,
            XenaException 
    {
        if (wrapper != null) 
        {
            wrapper.setParent(normaliser);
            wrapper.setProperty("http://xena/input", xis);
            wrapper.setProperty("http://xena/normaliser", normaliser);           
            normaliser.setContentHandler(wrapper);            
        }       
        return wrapper;
    }

    /**
     * Normalise an actual document given a source of data.
     * 
     * <p>
     * This method takes as a parameter a normaliser, an XIS and a wrapper and then
     * calls the normaliser's parse method on the XIS after the wrapper has been added
     * to the normaliser.
     * 
     * 
     * </p>
     * 
     * @param normaliser
     *            normaliser to use
     * @param xis
     *            source of data
     * @throws XenaException
     */
    public void parse(AbstractNormaliser normaliser, InputSource xis, AbstractMetaDataWrapper wrapper, NormaliserResults results)
            throws XenaException {
        try {
            
        	wrapper.startDocument();
            normaliser.parse(xis, results);
            wrapper.endDocument();
            
            // Don't bother the user with reporting success on every embedded
            // object!
            logger.finest(xis.getSystemId() + " successfully processed by "
                    + normaliser.toString());
        } catch (IOException x) {
            throw new XenaException(x);
        } catch (SAXException x) {
            throw new XenaException(x);
        } finally {
            try {
                if (xis instanceof XenaInputSource) {
                    ((XenaInputSource) xis).close();
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }

    public void complete() throws XenaException {
        final JarPreferences prefs = (JarPreferences) JarPreferences
                .userNodeForPackage(NormaliserManager.class);
        // Load the deactivated types list
        prefs.setClassLoader(pluginManager.getClassLoader());
        deactivatedInputTypes.putAll(getTypes(prefs,
                DEACTIVATED_INPUT_TYPES_STRING));
    }

    //    
    // /**
    // * This class is only here for debug purposes. the idea is that you can
    // * insert it into the stream of content handlers and it will give an error
    // * if you try and insert bad data.
    // */
    // private class MyFilter extends XMLFilterImpl {
    // /**
    // * I'm not sure why SAX doesn't enforce this, but it doesn't seem to.
    // * That means that without this, SAX could create bad XML.
    // */
    // public void characters(char ch[], int start, int length)
    // throws SAXException {
    // int end = start + length;
    // for (int i = start; i < end; i++) {
    // if (!isXMLCharacter(ch[i])) {
    // throw new SAXException("0x" + Integer.toHexString(ch[i])
    // + " is not a legal XML character");
    // }
    // }
    // super.characters(ch, start, length);
    // }
    //
    // /**
    // * Took this from org.jdom.Verifier
    // */
    // private boolean isXMLCharacter(char c) {
    // if (c == '\n') {
    // return true;
    // }
    // if (c == '\r') {
    // return true;
    // }
    // if (c == '\t') {
    // return true;
    // }
    // if (c < 0x20) {
    // return false;
    // }
    // if (c <= 0xD7FF) {
    // return true;
    // }
    // if (c < 0xE000) {
    // return false;
    // }
    // if (c <= 0xFFFD) {
    // return true;
    // }
    // if (c < 0x10000) {
    // return false;
    // }
    // if (c <= 0x10FFFF) {
    // return true;
    // }
    // return false;
    // }
    // }

    /**
     * normalise This code is part of the Xena API that should be used for all
     * applications that require Xena functionality (including, arguably, the
     * Xena GUI!) It should be called thusly: NormaliserManager
     * normaliserManager = NormaliserManager.singelton();
     * normaliserManager.normalise(normaliser, xis, log);
     * 
     * It is currently not called from the GUI, and changes wuill not effect the
     * GUI in any way.
     * 
     * @author aak
     * 
     * @param normaliser -
     *            normaliser to use
     * @param xis -
     *            the input source to use
     * @param destinationDir -
     *            destination dir for output files
     * @param fileNamer -
     *            fileNamer to use fo generate the output files
     * @param wrapper -
     *            wrapper to use to create the tags around the normalised
     *            content.
     * 
     * @return NormaliseDataStore - an object containing all the information
     *         generated during the normalise process.
     * @throws XenaException
     * @throws IOException
     * @throws SAXException
     */
    public NormaliserResults normalise(final XenaInputSource xis,
            final AbstractNormaliser normaliser, File destinationDir,
            AbstractFileNamer fileNamer, final AbstractMetaDataWrapper wrapper) throws XenaException,
            IOException {
        // check our arguments....
        if (xis == null) {
            throw new IllegalArgumentException(
                    "XenaInputSource must not be null.");
        }
        if (normaliser == null) {
            throw new IllegalArgumentException("Normaliser must not be null.");
        }
        if (destinationDir == null) {
            throw new IllegalArgumentException("Destination directory must not be null.");
        }
        if (fileNamer == null) {
            throw new IllegalArgumentException("File Namer must not be null.");
        }
        if (wrapper == null) {
            throw new IllegalArgumentException("Wrapper must not be null.");
        }

        // set up our thread correctly...
        ClassLoader deserLoader = pluginManager.getDeserClassLoader();
        Thread.currentThread().setContextClassLoader(deserLoader);

        // check to make sure our normaliser has a reference to a normaliser
        // manager (preferably this one!)
        if (normaliser.getNormaliserManager() == null) {
            normaliser.setNormaliserManager(this);
        }

        // create our results object
        NormaliserResults results = new NormaliserResults(xis, normaliser, destinationDir, fileNamer, wrapper);

        // create our output file...
        // TODO: should look at doing something with the file extension...
        File outputFile = fileNamer.makeNewXenaFile(xis, normaliser, destinationDir);
        
        xis.setOutputFileName(outputFile.getName());
        results.setOutputFileName(outputFile.getName());

        // create our transform handler
        TransformerHandler transformerHandler = null;
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try {
            transformerHandler = transformFactory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new XenaException(
                    "Unable to create transformerHandler due to transformer configuration exception.");
        }

        // TODO manage resorces better.

        OutputStream outputStream = new FileOutputStream(outputFile);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
            StreamResult streamResult = new StreamResult(osw);
            transformerHandler.setResult(streamResult);
        } catch (UnsupportedEncodingException e) {
            if (outputStream != null) {
                outputStream.close();
            }
            throw new XenaException(
                    "Unsupported encoder for output stream writer.");
        }

        // configure our normaliser
        normaliser.setProperty("http://xena/url", xis.getSystemId());
        normaliser.setProperty("http://xena/file", outputFile);
        normaliser.setProperty("http://xena/normaliser", normaliser);
        normaliser.setProperty("http://xena/input", xis);
        
//        normaliser.setContentHandler(transformerHandler);
        try 
        {
	        wrapper.setContentHandler(transformerHandler);
	        wrapTheNormaliser(normaliser, xis, wrapper);
	        
	        // do the normalisation!
//            normaliser.getContentHandler().startDocument();
            parse(normaliser, xis, wrapper, results);
//            normaliser.getContentHandler().endDocument();
            results.setNormalised(true);

            if (wrapper instanceof AbstractMetaDataWrapper) {
                AbstractMetaDataWrapper mde = (AbstractMetaDataWrapper) wrapper;
                String id = mde.getSourceId(new XenaInputSource(outputFile));
                results.setId(id);
            }
        } catch (XenaException x) {
            // JRW - delete xena file if exception occurs
            if (outputFile != null && outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputFile.delete();
            }

            // rethrow exception
            throw x;
        } catch (SAXException s) {
            // JRW - delete xena file if exception occurs
            if (outputFile != null && outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputFile.delete();
            }
            throw new XenaException(s);
        } catch (IOException iex) {
            // JRW - delete xena file if exception occurs
            if (outputFile != null && outputStream != null) {
                outputStream.flush();
                outputStream.close();
                outputFile.delete();
            }
            // rethrow exception
            throw iex;
        } finally {
            // let go the output files and any streams that are using it.
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            outputFile = null;
            normaliser.setProperty("http://xena/file", null);
            normaliser.setContentHandler(null);
            transformerHandler = null;
            System.gc();
        }
        return results;
    }

    /**
     * This is the export function for xena. It is called from the Xena object
     * as part of the Xena API.
     * 
     * Takes a xenaFile (which is just a regular file!), finds out what
     * normalised it, and tries to denormalise it. It calls the 
     * <code>export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles)</code>
     * method with overwrite set to false.
     * 
     * @see NormaliserManager.export(XenaInputSource xis, File outDir, boolean OverwriteExistingFiles)
     * @param xenaFile
     * @param outDir
     * @throws IOException
     * @throws SAXException
     * @throws XenaException
     * @throws ParserConfigurationException
     */

    public ExportResult export(XenaInputSource xis, File outDir)
            throws XenaException, IOException, SAXException,
            ParserConfigurationException {
        return export(xis, outDir, false);
    }    
    
    /**
     * 
     * Export a Xena file into either it's original format or the format it has
     * been converted to, depending on the normaliser / denormaliser. Some plugins
     * do not allow stuff to be denormalised for some reason.
     * 
     * <p>
     * This method takes a Xena file, looks up the abstract denormaliser 
     * 
     * 
     * 
     * </p>
     * 
     * @param xis
     * @param outDir
     * @param overwriteExistingFiles
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XenaException
     * @throws ParserConfigurationException
     */
    public ExportResult export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles)
        throws IOException, SAXException, XenaException, ParserConfigurationException {

        // get our export file name and call export(xis, outFile, overWrite)...
        
        // get the unwrapper for this package...
        XMLFilter unwrapper;
        try {
            unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);
        } catch (XenaException xe) {
            unwrapper = pluginManager.getMetaDataWrapperManager().getEmptyWrapper().getUnwrapper();
        }
            
        String tag = unwrapGetTag(xis, unwrapper);

        AbstractDeNormaliser deNormaliser = lookupDeNormaliser(tag);
        if (deNormaliser == null) {
            throw new XenaException("No Denormaliser available for type: " + tag);
        }

        String sourceSysId = pluginManager.getMetaDataWrapperManager().getSourceName(xis);

        URI uri = null;
        try {
            uri = new java.net.URI(sourceSysId);
        } catch (URISyntaxException x) {
            throw new XenaException(x);
        }
        String outFileName = "";
        try {
            outFileName = new File(uri).toString();
        } catch (IllegalArgumentException iae) {
            // there seems to have been a problem of some description. In this
            // case, we will
            // just get the system id, and take the last part of it for now...
            if (sourceSysId.lastIndexOf('/') != -1) {
                outFileName = sourceSysId.substring(sourceSysId.lastIndexOf('/'));
            }
            if (sourceSysId.lastIndexOf('\\') != -1) {
                outFileName = sourceSysId.substring(sourceSysId.lastIndexOf('\\'));
            }
        }

        if (outFileName == null || outFileName.length() == 0) 
        {
            throw new XenaException("Could not get output filename for some reason.");
        }
        
        String outputFileExtension = deNormaliser.getOutputFileExtension(xis);
        
        /*
         * This code adds the extension that the type gives us _if_ the name given to us
         * by the meta data wrapper does not have the same extension. This could happen in a
         * number of situations, most notably, for the plaintext, the default extension is
         * txt, however many file extensions are valid text files. at the end of the day, this
         * will at least reduce the instances of simple.txt -> simple.txt.txt and the like,
         * and still give a reasonable indication of what is actually in the file.
         * 
         */
        if (outputFileExtension != null) {
            if ( !outFileName.endsWith("." + outputFileExtension) ) {
                outFileName = outFileName + "." + outputFileExtension;
            }
        }
        return export(xis, outDir, outFileName, overwriteExistingFiles);
    }
    
    
    
    
    /**
     * This method allows Xena to export a file to 
     * 
     * @param xis
     * @param outDir
     * @param outFileName
     * @param overwriteExistingFiles
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws XenaException
     * @throws ParserConfigurationException
     */
    public ExportResult export(XenaInputSource xis, File outDir, String outFileName, boolean overwriteExistingFiles) 
        throws IOException, SAXException, XenaException, ParserConfigurationException {
        
        
        // first up - lets find out what is at the top of this xml file.
        // is there a package wrapper we know about or are we straight into a normaliser?
        // or it something we simply dont know about?
       
        XMLFilter unwrapper = null;
        String tag;
        try {
            unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);
            tag = unwrapGetTag(xis, unwrapper);
        } catch (XenaException xe) {
            // see if we can just get the tag regardless...
            tag = pluginManager.getMetaDataWrapperManager().getTag(xis);
        }
        
        AbstractDeNormaliser deNormaliser = lookupDeNormaliser(tag);
        if (deNormaliser == null) {
            throw new XenaException("No Denormaliser available for type: " + tag);
        }
        
        ExportResult result = new ExportResult();

        result.setInputSysId(xis.getSystemId());
        
        // TODO: aak - should this maybe arbitrarily start adding stuff to the
        // end of the file,
        // perhaps before the extension? who knows...
        // foo.txt foo_0001.txt foo_0002.txt etc... 
        // meh. figure it out later.
        File newFile = new File(outDir, outFileName);

        if (newFile.exists() && !overwriteExistingFiles) {
            throw new XenaException("File exists. Please remove before continuing.");
        }

        if (!newFile.getParentFile().exists()) {
            if (!newFile.getParentFile().mkdirs()) {
                throw new XenaException("Unable to create children folders!");
            }
        }
        result.setOutputFileName(outFileName);
        result.setOutputDirectoryName(outDir.getAbsolutePath());

        OutputStream outputStream = new FileOutputStream(newFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        StreamResult streamResult = new StreamResult(outputStream);
        streamResult.setWriter(outputStreamWriter);
        try {
            deNormaliser.setStreamResult(streamResult);
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            if (unwrapper != null) {
                unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);
                unwrapper.setParent(reader);
                unwrapper.setContentHandler(deNormaliser);
                reader.setContentHandler((ContentHandler) unwrapper);
            } else {
                reader.setContentHandler(deNormaliser);
            }
            reader.parse(xis);
            result.setExportSuccessful(true);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                xis.close();
            } catch (IOException x) {
                throw new XenaException(x);
            }
        }
        return result;
    }

    
    
    
    
    /**
     * @return Returns the pluginManager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * @param pluginManager
     *            The new value to set pluginManager to.
     */
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * @return Returns the normaliserMap.
     */
    public Map<String, AbstractNormaliser> getNormaliserMap() {
        return normaliserMap;
    }

}