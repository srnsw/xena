// $Id$

package com.jclark.xsl.om;

import java.util.Hashtable;

/**
 * manages collections of Names and NamespacePrefixMaps
 */
public class NameTableImpl implements NameTable 
{

    private NamespacePrefixMap emptyMap = new NamespacePrefixMapImpl();

    // a collection of NamespacePrefixMaps, (indexed by their hashcode?)
    // used for interning prefixMaps
    private Hashtable prefixMaps = new Hashtable();

    // a hashtable (keyed by namespace) of hashtables 
    //  (containing Names keyed by qNames)
    private Hashtable namespaces = new Hashtable();

    // the "no namespace" namespace
    private Hashtable docNamespace = new Hashtable();

    /**
     * Empty constructor initializes prefix map
     */
    public NameTableImpl() 
    {
        prefixMaps.put(emptyMap, emptyMap);
    }

    /**
     * construct (or find) a Name for the given qName in the 
     * given Namespace 
     */
    public Name createName(String qName, String namespace) 
    {
        Hashtable ns;
        synchronized (namespaces) {
            ns = (Hashtable)namespaces.get(namespace);
            if (ns == null) {
                ns = new Hashtable();
                namespaces.put(namespace, ns);
            }
        }
        return createName(ns, qName, namespace);
    }

    /**
     * create a name in the document's (null) namespace
     * for a non-qualified name
     */
    public Name createName(String nonQName) 
    {
        return createName(docNamespace, nonQName, null);
    }

    // 
    private NameImpl createName(Hashtable ns, String qName, 
                        String namespace) 
    {
        synchronized (ns) {
            // look in the hastable for the given qName
            NameImpl nm = (NameImpl)ns.get(qName);
            if (nm == null) {
                int i = qName.indexOf(':');
                // does the qName have a prefix?
                if (i == -1) {
                    // no, make a canonical Name
                    nm = new NameImpl(qName, namespace);
                } else {
                    // yes, make the canonical name, and one for this qname
                    // (both versions end up in the hashtable)
                    nm = new NameImpl(qName,
                                      namespace,
                                      createName(ns, qName.substring(i + 1), 
                                                 namespace));
                }
                ns.put(qName, nm);
            }
            return nm;
        }
    }

    /**
     *
     */
    public NamespacePrefixMap getEmptyNamespacePrefixMap() 
    {
        return emptyMap;
    }

    // ensure we don't carry duplicate maps
    NamespacePrefixMap intern(NamespacePrefixMap prefixMap)
    {
        synchronized (prefixMaps) {
            Object obj = prefixMaps.get(prefixMap);
            if (obj != null) {
                return (NamespacePrefixMap)obj;
            }
            prefixMaps.put(prefixMap, prefixMap);
            return prefixMap;
        }
    }


    //
    //
    //
    private final class NamespacePrefixMapImpl 
        implements NamespacePrefixMap 
    {
        // contains pairs of prefixes and namespaces
        final private String[] bindings;
        
        final private String defaultNS;
        
        NamespacePrefixMapImpl() 
        {
            this.bindings = new String[0];
            this.defaultNS = null;
        }
        
        private NamespacePrefixMapImpl(String[] bindings, 
                                       String defaultNS) 
        {
            this.bindings = bindings;
            this.defaultNS = defaultNS;
        }
        
        public final int getSize() 
        {
            // bindings contains two items for each binding
            return bindings.length >> 1;
        }
        
        public final String getPrefix(int i) 
        {
            return bindings[i << 1];
        }
        
        public final String getNamespace(int i) 
        {
            return bindings[(i << 1) | 1];
        }
        
        public final String getPrefix(String namespace) 
        {
            for (int i = 1; i < bindings.length; i += 2) {
                if (namespace.equals(bindings[i]))
                    return bindings[i - 1];
            }
            return null;
        }
        
        public final String getNamespace(String prefix) 
        {
            for (int i = 0; i < bindings.length; i += 2) {
                if (prefix.equals(bindings[i]))
                    return bindings[i + 1];
            }
            return null;
        }
        
        public final String getDefaultNamespace()
        {
            return defaultNS;
        }

        // qNames with no colon are in no namespace        
        public Name expandAttributeName(String name, 
                                        Node node) 
            throws XSLException 
        {
            int i = name.indexOf(':');
            if (i == -1) {
                return createName(name);
            }
            if (i == 3 && name.regionMatches(0, "xml", 0, 3)) {
                return createName(name, Name.XML_NAMESPACE);
            }
            for (int j = 0; j < bindings.length; j += 2) {
                String prefix = bindings[j];
                if ((prefix.length() == i) && 
                    (name.regionMatches(0, prefix, 0, i)) )
                    return createName(name, bindings[j + 1]);
            }
            throw new XSLException("no such prefix \"" + 
                                   name.substring(0, i) + '"', node);
        }

        
        // just like above, but we'll take the default namespace
        // if there's no prefix
        public Name expandElementTypeName(String name, 
                                          Node node) 
            throws XSLException 
        {
            int i = name.indexOf(':');
            if (i == -1) {
                if (defaultNS != null) {
                    return createName(name, defaultNS);
                } else {
                    return createName(name);
                }
            }
            if (i == 3 && name.regionMatches(0, "xml", 0, 3)) {
                return createName(name, Name.XML_NAMESPACE);
            }
            for (int j = 0; j < bindings.length; j += 2) {
                String prefix = bindings[j];
                if ((prefix.length() == i) && 
                    (name.regionMatches(0, prefix, 0, i)) ) {
                    return createName(name, bindings[j + 1]);
                }
            }
            throw new XSLException("no such prefix \"" + 
                                   name.substring(0, i) + '"', node);
        }
        
        // remove a prefix association
        public NamespacePrefixMap unbind(String prefix) 
        {
            for (int i = 0; i < bindings.length; i += 2) {
                if (prefix.equals(bindings[i])) {
                    String[] newBindings = 
                        new String[bindings.length - 2];
                    System.arraycopy(bindings, 0, 
                                     newBindings, 0, i);
                    System.arraycopy(bindings, i + 2, 
                                     newBindings, i, 
                                     bindings.length - i - 2);
                    return intern(new NamespacePrefixMapImpl(newBindings,
                                                             defaultNS));
                }
            }
            return this;
        }
        
        public NamespacePrefixMap bind(String prefix, String namespace) 
        {
            int i;
            for (i = 0; i < bindings.length; i += 2) {
                int cmp = prefix.compareTo(bindings[i]);
                if (cmp < 0) {
                    break;
                }
                if (cmp == 0) {
                    if (namespace.equals(bindings[i + 1])) {
                        return this;
                    }
                    String[] newBindings = (String[])bindings.clone();
                    newBindings[i + 1] = namespace;
                    return intern(new NamespacePrefixMapImpl(newBindings, defaultNS));
                }
            }
            String[] newBindings = new String[bindings.length + 2];
            System.arraycopy(bindings, 0, newBindings, 0, i);
            newBindings[i] = prefix;
            newBindings[i + 1] = namespace;
            System.arraycopy(bindings, i, newBindings, i + 2,
                             bindings.length - i);
            return intern(new NamespacePrefixMapImpl(newBindings, defaultNS));
        }
        
        public NamespacePrefixMap bindDefault(String namespace) 
        {
            if (namespace.equals(defaultNS)) {
                return this;
            }
            return intern(new NamespacePrefixMapImpl(bindings, namespace));
        }
        
        public NamespacePrefixMap unbindDefault()
        {
            if (defaultNS == null) {
                return this;
            }
            return intern(new NamespacePrefixMapImpl(bindings, null));
        }
        
        public NameTable getNameTable() 
        {
            return NameTableImpl.this;
        }
        
        public int hashCode() 
        {
            int h = defaultNS != null ? defaultNS.hashCode() : 0;
            for (int i = 0; i < bindings.length; i++) {
                h ^= bindings[i].hashCode();
            }
            return h;
        }
        
        public boolean equals(Object obj)
        {
            if (obj == null || !(obj instanceof NamespacePrefixMapImpl)) {
                return false;
            }
            NamespacePrefixMapImpl map = (NamespacePrefixMapImpl)obj;
            if (defaultNS == null) {
                if (map.defaultNS != null) {
                    return false;
                }
            }
            else if (!defaultNS.equals(map.defaultNS)) {
                return false;
            }
            if (bindings.length != map.bindings.length) {
                return false;
            }
            for (int i = 0; i < bindings.length; i++) {
                if (!bindings[i].equals(map.bindings[i])) {
                    return false;
                }
            }
            return true;
        }
    }


    //
    //
    //


    private class NameImpl implements Name 
    {
        private String qName;
        private String namespace;
        private NameImpl canon;


        // qame is assumed to have no prefix, here
        NameImpl(String qName, String namespace) 
        {
            // WDL debug
//              if (qName == null) {
//                  try {
//                      throw new Exception(" null qName in ns: " + namespace);
//                  } catch (Exception ex) {
//                      ex.printStackTrace();
//                  }
//              }
                
            this.qName = qName;
            this.namespace = namespace;
            this.canon = this;
        }
        
        // our qname has a prefix, so we get the canonical version
        // provided as well
        NameImpl(String qName, String namespace, NameImpl canon) 
        {
            // WDL debug
//              if (qName == null) {
//                  try {
//                      throw new Exception(" null qName in ns: " + namespace);
//                  } catch (Exception ex) {
//                      ex.printStackTrace();
//                  }
//              }

            this.qName = qName;
            this.namespace = namespace;
            this.canon = canon;
        }
        
        public String getNamespace()
        {
            return namespace;
        }

        public String getLocalPart() 
        {
            return canon.qName;
        }

        public String getPrefix() 
        {
            int i = qName.indexOf(':');
            if (i < 0) {
                return null;
            }
            return qName.substring(0, i);
        }
        
        public boolean equals(Object obj) 
        {
            if (obj != null && (obj instanceof NameImpl)) {
                return ((NameImpl)obj).canon == canon;
            }
            return false;
        }

        public String toString() 
        {
            return qName;
        }

        public int hashCode() 
        {
            return System.identityHashCode(canon);
        }
        
        public Object getCreator() 
        {
            return NameTableImpl.this;
        }
    }


}
