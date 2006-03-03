package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileNotFoundException;
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
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.XenaWrapper;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Manages all the available Normalisers.
 * 
 * @see Normaliser
 * @see XMLReader
 * @author Chris
 * @created April 8, 2002
 */
public class NormaliserManager implements LoadManager {
    public final static String PREF_AUTO_LOG = "autoLog";

    public final static String SOURCE_DIR_STRING = "sourceDirectory";

    public final static String DESTINATION_DIR_STRING = "destinationDirectory";

    public final static String ERROR_DIR_STRING = "errorDirectory";

    public final static String CONFIG_DIR_STRING = "configDirectory";

    public final static String DEACTIVATED_INPUT_TYPES_STRING = "deactivatedInputTypes";

    protected static NormaliserManager theSingleton = new NormaliserManager();

    protected Map<Object, List<Class>> fromMap = new HashMap<Object, List<Class>>();

    protected Map<String, Class> nameMap = new HashMap<String, Class>();

    protected Set<Class> all = new HashSet<Class>();

    protected Map<String, List<Class>> denormaliserTagMap = new HashMap<String, List<Class>>();

    /**
     * Map<XMLReader, Set<Type>>
     */
    protected Map<Class, Set<Type>> outputTypes = new HashMap<Class, Set<Type>>();

    /**
     * Map<XMLReader, Set<Type>>
     */
    protected Map<Class, Set<Type>> inputTypes = new HashMap<Class, Set<Type>>();

    /**
     * Map<XMLReader, Set<Type>>
     */
    protected Map<Class, Set<Type>> deactivatedInputTypes = 
    	new HashMap<Class, Set<Type>>();

    private Map<String, AbstractNormaliser> normaliserMap = new HashMap<String, AbstractNormaliser>();

    private Map<String, AbstractDeNormaliser> denormaliserMap = new HashMap<String, AbstractDeNormaliser>();

    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    public static NormaliserManager singleton() {
        return theSingleton;
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
    public List<XMLReader> getAllReaders() {
        List<XMLReader> rtn = new ArrayList<XMLReader>();
        Iterator it = all.iterator();
        while (it.hasNext()) {
            Class cls = (Class) it.next();
            if (Reflect.conformsTo(cls, XMLReader.class)) {
                try {
                    rtn.add((XMLReader)cls.newInstance());
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
    protected Map<Class, Set<Type>> getTypes(JarPreferences prefs, String mapName)
            throws XenaException {
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
                    cls = PluginManager.singleton().getDeserClassLoader().loadClass(normName);
                } catch (ClassNotFoundException e) {
                    // ignore... throw exception if cls is still null.
                }
            }
            if (cls == null) {
                throw new XenaException("Unable to load class: " + normName);
            }
            typesMap.put(cls, list);
            //load the class.
            try {
                Object classInstance = cls.newInstance();
                if (classInstance instanceof AbstractNormaliser) {
                    normaliserMap.put(classInstance.toString(),(AbstractNormaliser) classInstance);
                } else if (classInstance instanceof AbstractDeNormaliser) {
                    denormaliserMap.put(classInstance.toString(),(AbstractDeNormaliser) classInstance);
                }
            } catch (Exception e) {
                //sysout - print exception if class unable to be instantiated and added to the list.
                System.out.println("Class ["+ normName + "] was not able to be added to the normaliser / denormaliser lists for some reason.");
                e.printStackTrace();
            }
            if (!typeToNormaliserComponent.hasMoreTokens()) {
                throw new XenaException("Bad normaliserMap: " + typeToNormaliserString);
            }
            String types = typeToNormaliserComponent.nextToken();
            StringTokenizer st3 = new StringTokenizer(types, ",");
            while (st3.hasMoreTokens()) {
                String typeName = st3.nextToken();
                Type type = TypeManager.singleton().lookupByClassName(typeName);
                if (type == null) {
                    throw new XenaException("Bad normaliserMap, unknown type: " + typeName + " ... " + typeToNormaliserString);
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
            
            if (output == null) {
                throw new XenaException("Error: outputMap for: " + 
                        norm.getClass().getName() + 
                        "is null!");
            }
            if (output.size() != 1) {
                throw new XenaException("Error: outputMap for: " + 
                        norm.getClass().getName() + " has: " + 
                        output.size() +
                        " but must have exactly one entry per normaliser");
            }

            if (input == null) {
                throw new XenaException("Error: inputMap for: " + 
                        norm.getClass().getName() + 
                        "is null!");
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
    public Set<Type> getDeactivatedInputTypes(Class cls) {
        return deactivatedInputTypes.get(cls);
    }

    /**
     * Return the Map of deactivated input types
     * 
     * @return Map<Class, List<Type>>
     */
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

        
        if (normaliser instanceof TransformerHandler
                && type instanceof XenaFileType) {
            XenaFileType xft = (XenaFileType) type;

            System.out.println("Have a xena filetype: " + xft.getName());
            
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
    public TransformerHandler lookupDeNormaliser(String tag) {
        try {
            List l = (List) denormaliserTagMap.get(tag);
            if (l == null) {
                return null;
            }
            Class cls = (Class) l.get(0);
            return (TransformerHandler) cls.newInstance();
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
    public TransformerHandler lookupDeNormaliser(Type type) {
        List list = lookupList(type);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Class cls = (Class) it.next();
            if (Reflect.conformsTo(cls, TransformerHandler.class)) {
                try {
                    return (TransformerHandler) cls.newInstance();
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
     * Activate or deactivate the acceptance of particular types for a
     * normaliser
     * 
     * @param normaliser
     *            XMLReader Class object
     * @param type
     *            type to activate or deactivate
     * @param activate
     *            whether to activate or deactivate
     */
    public void activateType(Class normaliser, Type type, boolean activate)
            throws XenaException {
        Set types = (Set) inputTypes.get(normaliser);
        Set<Type> dtypes = deactivatedInputTypes.get(normaliser);
        if (!types.contains(type)) {
            throw new XenaException(
                    "Internal Error: trying to deactivate type "
                            + type.getClass());
        }
        if (activate) {
            if (dtypes != null) {
                dtypes.remove(type);
            }
            if (dtypes.size() == 0) {
                deactivatedInputTypes.remove(normaliser);
            }
        } else {
            if (dtypes == null) {
                dtypes = new HashSet<Type>();
                deactivatedInputTypes.put(normaliser, dtypes);
            }
            dtypes.add(type);
        }
        synchDeactivatedPrefs();
    }

    /**
     * Write the internal cache of deactivated types to the users preferences
     */
    protected void synchDeactivatedPrefs() {
        JarPreferences prefs = (JarPreferences) JarPreferences
                .userNodeForPackage(NormaliserManager.class);
        Iterator nit = NormaliserManager.singleton().getAllReaders().iterator();
        StringBuffer val = new StringBuffer();
        boolean firstReader = true;
        while (nit.hasNext()) {
            XMLReader reader = (XMLReader) nit.next();
            Set<Type> dtypes = deactivatedInputTypes.get(reader.getClass());
            if (dtypes != null && dtypes.size() != 0) {
                if (!firstReader) {
                    val.append(" ");
                }
                firstReader = false;
                val.append(reader.getClass().getName());
                val.append("/");
                Iterator it = dtypes.iterator();
                boolean firstType = true;
                while (it.hasNext()) {
                    Type atype = (Type) it.next();
                    if (!firstType) {
                        val.append(",");
                    }
                    firstType = false;
                    val.append(atype.getClass().getName());
                }
            }
        }
        prefs.put(DEACTIVATED_INPUT_TYPES_STRING, val.toString());
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
    public String unwrapGetTag(XenaInputSource xis, XMLFilter unwrapper) throws XenaException {
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            unwrapper.setContentHandler(new XMLFilterImpl() {
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    
                    // Bail out early as soon as we've found what we want
                    // for super efficiency.
                    //notout
                    //System.out.println("found tag -> local name: " + localName + " qName: " + qName);
                   throw new FoundException(localName, qName);
                }
            });
            reader.setContentHandler((ContentHandler) unwrapper);
            //notout
            //System.out.println("About to perform parse... on xis: " + xis.toString());
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
            XMLFilter unwrapper = MetaDataWrapperManager.singleton().getUnwrapNormaliser();
            unwrapper.setParent(reader);
            unwrapper.setContentHandler(new XMLFilterImpl() {
                
                public void startElement(String uri, String localName, String qName, Attributes attributes)
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
            if (e.qtag == null || e.qtag.equals("")) {
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
     * Get the outermost XML tag from a Xena document
     * TODO: Should this be in here? i mean, this isnt really anything to with normalising is it?
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
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        XMLFilter unwrapper = MetaDataWrapperManager.singleton().getUnwrapNormaliser();
        unwrapper.setParent(reader);
        unwrapper.setContentHandler(ch);
        InputSource is = new InputSource(systemid);
        reader.setContentHandler((ContentHandler) unwrapper);
        reader.parse(is);
    }

    // JRW removed XenaResultsLog

    public void newInputSource(XenaInputSource input) {
        // Nothing. Put in for DPR
    }

    /**
     * Close the output stream. Having a function just for this may be overkill,
     * but it helps the DPR know what is going on.
     */
    public void closeOutputHandler(NormaliserDataStore ns) throws IOException {
        if (ns.getOut() != null) {
            ns.getOut().close();
        }
    }

    /**
     * Sets up the basic housekeeping prior to creating a Xena file.
     * 
     * @param normaliser
     *            the Xena normaliser that will be doing the work
     * @param input
     *            the source of the data
     * @param overwrite
     *            if we should overwrite any existing file
     * @return NormaliserStream object containing all the relevant setup objects
     */
    public NormaliserDataStore newOutputHandler(XMLReader normaliser, XenaInputSource input, boolean overwrite)
        throws XenaException, IOException, SAXException {
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                    .newInstance();
            TransformerHandler writer = tf.newTransformerHandler();
            normaliser.setProperty("http://xena/url", input.getSystemId());
            normaliser.setContentHandler(writer);
            FileNamer fn = FileNamerManager.singleton().getFileNamerFromPrefs();
            if (fn == null) {
                throw new XenaException(
                        "No File Namer Found. Go to Properties to update.");
            }
            File xenaFile = fn.makeNewXenaFile(normaliser, input,
                    FileNamer.XENA_DEFAULT_EXTENSION);
            File cfgFile = fn.makeNewXenaFile(normaliser, input,
                    FileNamer.XENA_CONFIG_EXTENSION);
            normaliser.setProperty("http://xena/file", xenaFile);
            normaliser.setProperty("http://xena/normaliser", normaliser);
            normaliser.setProperty("http://xena/input", input);
            OutputStream out = null;
            boolean existsAlready = true;
            if (overwrite || !xenaFile.exists()) {
                existsAlready = false;
                out = new FileOutputStream(xenaFile);
                OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
                StreamResult streamResult = new StreamResult(osw);
                writer.setResult(streamResult);
            }
            return new NormaliserDataStore(writer, xenaFile, cfgFile, out, existsAlready);
        } catch (TransformerConfigurationException x) {
            throw new XenaException(x);
        }
    }

    /**
     * Return a ContentHandler with suitable chaining of events so that the
     * output file will be wrapped with appropriate meta-data
     * 
     * @param normaliser
     *            normaliser to be used
     * @param xis
     *            source of data
     * @param mesgLevel
     *            level of embedding within the output Xena file
     * @return ContentHandler ContentHandler which will handle the XML stream
     */
    public ContentHandler wrapTheNormaliser(XMLReader normaliser, XenaInputSource xis)
    throws SAXException, XenaException {
        
        XMLFilter filter = MetaDataWrapperManager.singleton().getActiveWrapperPlugin().getWrapper();
        return (ContentHandler)  wrapTheNormaliser(normaliser,  xis,  filter);

        /*
        if (filter != null) {
            filter.setParent(normaliser);
            filter.setProperty("http://xena/input", xis);
            filter.setProperty("http://xena/normaliser", normaliser);
            XMLFilterImpl embFilter = new XMLFilterImpl() {
                public void startDocument() {
                };

                public void endDocument() {
                };
            };
            filter.setContentHandler((ContentHandler) embFilter);
            if (normaliser.getContentHandler() != null) {
                embFilter.setContentHandler(normaliser.getContentHandler());
            }
            embFilter.setParent(filter);
        }
        return (ContentHandler) filter;
        */
        
    }

    /**
     * Return a ContentHandler with suitable chaining of events so that the
     * output file will be wrapped with appropriate meta-data
     * 
     * @param normaliser - normaliser to be used
     * @param xis - source of data
     * @param wrapper - xml wrapper
     * @return ContentHandler ContentHandler which will handle the XML stream
     */
    public ContentHandler wrapTheNormaliser(XMLReader normaliser, XenaInputSource xis, XMLFilter wrapper) throws SAXException,
            XenaException {
        if (wrapper != null) {
            
            //notout
            //System.out.println("Wrapper:" + wrapper.toString());
            
            wrapper.setParent(normaliser);
            wrapper.setProperty("http://xena/input", xis);
            wrapper.setProperty("http://xena/normaliser", normaliser);
            
            XMLFilterImpl embFilter = new XMLFilterImpl() {
                public void startDocument() {
                };

                public void endDocument() {
                };
            };
            wrapper.setContentHandler((ContentHandler) embFilter);
            if (normaliser.getContentHandler() != null) {
                embFilter.setContentHandler(normaliser.getContentHandler());
            }
            embFilter.setParent(wrapper);
        }
        return (ContentHandler) wrapper;
    }
    
    
    
    /**
     * Normalise an actual document given a source of data Take into account the
     * fact that there may not actually be a log...
     * 
     * @param normaliser
     *            normaliser to use
     * @param xis
     *            source of data
     * @throws XenaException
     */
    public void parse(XMLReader normaliser, InputSource xis,  XMLFilter wrapper) throws XenaException {
        try {
            ContentHandler filter = wrapTheNormaliser(normaliser,(XenaInputSource) xis, wrapper);
            if (filter != null) {
                ((ContentHandler) filter).startDocument();
            }
            normaliser.parse(xis);
            if (filter != null) {
                ((ContentHandler) filter).endDocument();
            }
            // Don't bother the user with reporting success on every embedded
            // object!
            logger.finest(xis.getSystemId() + " successfully processed by " + normaliser.toString());
        } catch (IOException x) {
            //System.err.println("Normaliser manager has caught exception.");
            throw new XenaException(x);
        } catch (SAXException x) {
            //System.err.println("Normaliser manager has caught exception.");
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


    public void setDeactivatedInputTypes(Map<Class, Set<Type>> deactivatedInputTypes) {
        this.deactivatedInputTypes = deactivatedInputTypes;
        synchDeactivatedPrefs();
    }

    public void complete() throws XenaException {
        final JarPreferences prefs = (JarPreferences) JarPreferences
                .userNodeForPackage(NormaliserManager.class);
        // Load the deactivated types list
        prefs.setClassLoader(PluginManager.singleton().getClassLoader());
        deactivatedInputTypes.putAll(getTypes(prefs,DEACTIVATED_INPUT_TYPES_STRING));
    }

    
//    
//    /**
//     * This class is only here for debug purposes. the idea is that you can
//     * insert it into the stream of content handlers and it will give an error
//     * if you try and insert bad data.
//     */
//    private class MyFilter extends XMLFilterImpl {
//        /**
//         * I'm not sure why SAX doesn't enforce this, but it doesn't seem to.
//         * That means that without this, SAX could create bad XML.
//         */
//        public void characters(char ch[], int start, int length)
//                throws SAXException {
//            int end = start + length;
//            for (int i = start; i < end; i++) {
//                if (!isXMLCharacter(ch[i])) {
//                    throw new SAXException("0x" + Integer.toHexString(ch[i])
//                            + " is not a legal XML character");
//                }
//            }
//            super.characters(ch, start, length);
//        }
//
//        /**
//         * Took this from org.jdom.Verifier
//         */
//        private boolean isXMLCharacter(char c) {
//            if (c == '\n') {
//                return true;
//            }
//            if (c == '\r') {
//                return true;
//            }
//            if (c == '\t') {
//                return true;
//            }
//            if (c < 0x20) {
//                return false;
//            }
//            if (c <= 0xD7FF) {
//                return true;
//            }
//            if (c < 0xE000) {
//                return false;
//            }
//            if (c <= 0xFFFD) {
//                return true;
//            }
//            if (c < 0x10000) {
//                return false;
//            }
//            if (c <= 0x10FFFF) {
//                return true;
//            }
//            return false;
//        }
//    }

    public OutputStream openAutoLog() throws XenaException,
            FileNotFoundException {
        final JarPreferences prefs = (JarPreferences) JarPreferences
                .userNodeForPackage(NormaliserManager.class);
        File configDir = null;
        configDir = LegacyXenaCode.getBaseDirectory(CONFIG_DIR_STRING);
        if (configDir != null) {
            File autoLog = new File(configDir, "log.txt");
            if (prefs.getBoolean(NormaliserManager.PREF_AUTO_LOG, false)) {
                OutputStream os = new FileOutputStream(autoLog, true);
                return os;
            }
        }
        return null;
    }

    public void closeAutoLog(OutputStream os) throws IOException {
        if (os != null) {
            os.close();
        }
    }

    public void writeAutoLog(OutputStream os, String str) throws IOException {
        if (os != null) {
            os.write(str.getBytes());
            os.flush();
        }
    }
    
    
    /**
     * normalise
     * This code is part of the Xena API that should be used for all
     * applications that require Xena functionality (including, arguably, the
     * Xena GUI!) It should be called thusly:
     * NormaliserManager normaliserManager = NormaliserManager.singelton(); 
     * normaliserManager.normalise(normaliser, xis, log); 
     * 
     * It is currently not called from the GUI, and changes wuill not effect the GUI in any way.
     * 
     * @author aak
     * 
     * @param normaliser - normaliser to use
     * @param xis - the input source to use
     * @param destinationDir - destination dir for output files
     * @param fileNamer - fileNamer to use fo generate the output files
     * @param wrapper - wrapper to use to create the tags around the normalised content.
     * 
     * @return NormaliseDataStore - an object containing all the information generated during the normalise process.
     * @throws XenaException
     * @throws IOException
     * @throws SAXException
     */
    public NormaliserResults normalise(final XenaInputSource xis,
                                         final AbstractNormaliser normaliser,
                                         File destinationDir,
                                         FileNamer fileNamer,
                                         final XMLFilter wrapper)
    throws XenaException, IOException {
        //check our arguments....
        if (xis == null) {
            throw new IllegalArgumentException("XenaInputSource must not be null.");
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

        //set up our thread correctly...

        ClassLoader deserLoader = PluginManager.singleton().getDeserClassLoader();
        Thread.currentThread().setContextClassLoader(deserLoader);
        
        //create our results object
        NormaliserResults results = new NormaliserResults(xis, normaliser, destinationDir, fileNamer, wrapper);
        
        // create our output file...
        //TODO: should look at doing something with the file extension...
        File outputFile = fileNamer.makeNewXenaFile(normaliser, xis, FileNamer.XENA_DEFAULT_EXTENSION, destinationDir);
        results.setOutputFileName(outputFile.getName());
        
        
        //create our transform handler
        TransformerHandler transformerHandler = null;
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try {
            transformerHandler = transformFactory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new XenaException("Unable to create transformerHandler due to transformer configuration exception.");
        }
        
        //TODO manage resorces better.
        
        //notout
        //System.out.println("output file:" + outputFile);
        OutputStream out = new FileOutputStream(outputFile);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            StreamResult streamResult = new StreamResult(osw);
            transformerHandler.setResult(streamResult);
        } catch (UnsupportedEncodingException e) {
            if (out != null) {
                out.close();
            }
            throw new XenaException("Unsupported encoder for output stream writer.");
        }
        
        //configure our normaliser
    	normaliser.setProperty("http://xena/url", xis.getSystemId());
    	normaliser.setContentHandler(transformerHandler);
    	normaliser.setProperty("http://xena/file", outputFile);
    	normaliser.setProperty("http://xena/normaliser", normaliser);
    	normaliser.setProperty("http://xena/input", xis);

        //do the normalisation!
        try {
            normaliser.getContentHandler().startDocument();
            parse(normaliser, xis, wrapper);
            normaliser.getContentHandler().endDocument();
            results.setNormalised(true);
            
            if (wrapper instanceof XenaWrapper) {
                XenaWrapper xe = (XenaWrapper)wrapper;
                String id = xe.getSourceId(new XenaInputSource(outputFile));
                results.setId(id);
            }
            
        } catch (XenaException x) {
            x.printStackTrace();
        	// JRW - delete xena file if exception occurs
        	if (outputFile != null && out != null)
        	{
        		out.flush();
        		out.close();
        		outputFile.delete();
        	}
        	
        	// rethrow exception
            throw x;
        } catch (SAXException s) {
        	s.printStackTrace();
            // JRW - delete xena file if exception occurs
            if (outputFile != null && out != null)
            {
                out.flush();
                out.close();
                outputFile.delete();
            }
        	throw new XenaException(s);
        } catch (IOException iex) {
        	// JRW - delete xena file if exception occurs
            
        	if (outputFile != null && out != null)
        	{
        		out.flush();
        		out.close();
        		outputFile.delete();
        	}
        	// rethrow exception
        	throw iex;
        } finally {
            //let go the output files and any streams that are using it.

            
//            if (outputFile != null) {
//                outputFile.delete();
//            }
            
            if (out != null) {
        		out.flush();
                out.close();
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
     * This is the export function for xena. It is called from the Xena object as part of the Xena API.
     * 
     * Takes a xenaFile (which is just a regular file!), finds out what normalised it, and tries to denormalise it.
     * @param xenaFile
     * @param outDir
     * @throws IOException
     * @throws SAXException
     * @throws XenaException
     * @throws ParserConfigurationException
     */
    
    public ExportResult export(XenaInputSource xis, File outDir) throws XenaException, IOException, SAXException, ParserConfigurationException {
        return export(xis, outDir, false);
    }
    
    
    public ExportResult export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles) throws IOException, SAXException, XenaException, ParserConfigurationException {
        //notout
        //System.out.println("Starting export...");
        
        ExportResult result = new ExportResult();
        
        //get the unwrapper for this package...
        XMLFilter unwrapper = PluginManager.singleton().getMetaDataWrapperManager().getUnwrapper(xis);
        
        String tag = unwrapGetTag(xis, unwrapper);
        
        //sysout
        System.out.println("tag: " + tag);
        TransformerHandler transformerHandler = lookupDeNormaliser(tag);
        if (transformerHandler == null) {
            throw new XenaException("No Denormaliser available for type: " + tag);
        }
        //notout
        //System.out.println("Transformed handler is an instance of " + transformerHandler.getClass().getName());
        FileType type = (FileType)getOutputType(transformerHandler.getClass());
        
        String sysId = MetaDataWrapperManager.singleton().getSourceName(xis);
        
        //notout
        //System.out.println(sysId);
        result.setInputFileName(sysId);
        
        URI uri = null;
        try {
            uri = new java.net.URI(sysId);
        } catch (URISyntaxException x) {
            throw new XenaException(x);
        }
        //notout
        //System.out.println("URI: " + uri);
        String outFileName = "";
        try {
            outFileName = new File(uri).toString();
        } catch (IllegalArgumentException iae) {
            //there seems to have been a problem of some description. In this case, we will
            // just get the system id, and take the last part of it for now...
            
            //String fullSysId = sysId;
            if (sysId.lastIndexOf('/') != -1 ) {
                outFileName = sysId.substring(sysId.lastIndexOf('/'));                
            }
            if (sysId.lastIndexOf('\\') != -1 ) {
                outFileName = sysId.substring(sysId.lastIndexOf('\\'));
            }
            
        }
            
        
        /* TODO: aak - perhaps this should be modified not to write the file extension after
         * the file name if the file name already ends in that extension,
         * ie foo.txt should not have '.txt' appended to it to make it 'foo.txt.txt'
         */
        if (type.fileExtension() != null) {
            outFileName = outFileName + "." + type.fileExtension();
        }
        
        //TODO: aak - should this maybe arbitrarily start adding stuff to the end of the file, 
        // perhaps before the extension? who knows...
        // foo.txt foo_0001.txt foo_0002.txt etc... meh. figure it out later.
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
            transformerHandler.setResult(streamResult);
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            unwrapper = MetaDataWrapperManager.singleton().getUnwrapper(xis);
            unwrapper.setParent(reader);
            unwrapper.setContentHandler(transformerHandler);
            reader.setContentHandler((ContentHandler) unwrapper);
            reader.parse(xis);
            result.setExportSuccessful(true);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException x) {
                throw new XenaException(x);
            }
        }
        //notout
        //System.out.println("Returning! who would have thought eh?");
        return result;
    }
    
    
    
    
    /**
     * @return Returns the denormaliserMap.
     */
    public Map<String, AbstractDeNormaliser> getDenormaliserMap() {
        return denormaliserMap;
    }

    /**
     * @return Returns the normaliserMap.
     */
    public Map<String, AbstractNormaliser> getNormaliserMap() {
        return normaliserMap;
    }

}
