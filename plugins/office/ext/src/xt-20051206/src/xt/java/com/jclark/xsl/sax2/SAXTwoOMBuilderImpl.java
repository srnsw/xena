// $Id$

package com.jclark.xsl.sax2;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;


import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXParseException;

import com.jclark.xsl.om.*;

import com.jclark.xsl.tr.Result;
import com.jclark.xsl.tr.LoadContext;


/**
 *  constructs an (xslt) XML object model from SAX2 events
 * N.B. Namespaces and NamespacePrefixes parameters should be set to 
 * <code>true</code> in the XMLReader 
 */
public class SAXTwoOMBuilderImpl implements SAXTwoOMBuilder 
{
    char[] _dataBuf = new char[1024];
    int _dataBufUsed = 0;
    RootNodeImpl _rootNode = null;
    ContainerNodeImpl _currentNode;
    int _currentIndex = 1;
    
    boolean _includeProcessingInstructions;
    boolean _includeComments;
    LoadContext _context;
    
    InputSource _input;
    Locator _locator = new NullLocator();

    Hashtable _pendingNamespaces = new Hashtable();

    
    /**
     * make sure you call init() if you construct with this method
     */
    SAXTwoOMBuilderImpl()
    {}

    SAXTwoOMBuilderImpl(LoadContext context, 
                        String systemId, 
                        int documentIndex,  
                        NamespacePrefixMap nsMap,
                        InputSource input) 
    {
        init(context, systemId, documentIndex, nsMap, input);
    }

    public void init (LoadContext context, 
                      String systemId, 
                      int documentIndex,  
                      NamespacePrefixMap nsMap,
                      InputSource input) 
    {
        _context = context;
        _input = input;
        _includeProcessingInstructions = 
            _context.getIncludeProcessingInstructions();
        
        _includeComments = _context.getIncludeComments();

        _currentNode = _rootNode = new RootNodeImpl(systemId,
                                                    documentIndex,
                                                    nsMap);
    }

    public void startDocument() 
    { 
        if (_rootNode.getURL() == null && _input != null) {
            String newSysId = _input.getSystemId();
            _rootNode.setSystemId(newSysId);
        }
    }
    
    public void endDocument() 
    { }
        
    public void setDocumentLocator(Locator locator) 
    {
        _locator = locator;
    }
        
    public void startPrefixMapping(String prefix, String uri)
    {
        _pendingNamespaces.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix)
    {}

    public void startElement(String namespaceURI,
                             String localName, String qName,
                             Attributes atts)
        throws SAXException
    {
        flushData();
        ElementNodeImpl element;
        boolean preserve;
            
        String space = atts.getValue("xml:space");
        String havelocator = (_locator == null) ? "n" : "y";

        if (space == null) {
            preserve = _currentNode.preserveSpace();
        }
        else if (space.equals("default")) {
            preserve = false;
        } else if (space.equals("preserve")) {
            preserve = true;
        } else {
            preserve = _currentNode.preserveSpace();
        }
        try {
            if (preserve) {
                element = new PreserveElementNodeImpl(namespaceURI, qName, atts, _locator,
                                                      _currentIndex++, _currentNode, _pendingNamespaces);
            } else {
                element = new ElementNodeImpl(namespaceURI, qName, atts, _locator,
                                              _currentIndex++, _currentNode, _pendingNamespaces);
            }
        } catch (XSLException e) {
            throw new SAXException(e);
        }
        _currentIndex += atts.getLength();
        _currentNode = element;
        _pendingNamespaces.clear();
    }
    
    public void characters(char ch[], int start, int length)
    {
        int need = length + _dataBufUsed;
        if (need > _dataBuf.length) {
            int newLength = _dataBuf.length << 1;
            while (need > newLength) {
                newLength <<= 1;
            }
            char[] tem = _dataBuf;
            _dataBuf = new char[newLength];
            if (_dataBufUsed > 0)
                System.arraycopy(tem, 0, _dataBuf, 0, _dataBufUsed);
        }
        for (; length > 0; length--) {
            _dataBuf[_dataBufUsed++] = ch[start++];
        }
    }
        
    public void ignorableWhitespace(char ch[], int start, int length)
    {
        if (_dataBufUsed > 0 || 
            _currentNode.preserveSpace() || 
            ! _context.getStripSource(_currentNode.getName())) {

            characters(ch, start, length);
        }
    }
        
    public void endElement(String namespaceURI, String localname, String qName)
    {
        flushData();
        _currentNode = _currentNode.parent;
    }

    public void skippedEntity(String entityName)
    {}
        
    public void processingInstruction(String target, String data) 
    {
        if (target == null) {
            comment(data);
        } else {
            if (_includeProcessingInstructions) {
                flushData();
                new ProcessingInstructionNodeImpl(target, data, _locator,
                                                  _currentIndex++, _currentNode);
            }
        }
    }
        
    public void comment(String contents)
    {
        if (_includeComments) {
            flushData();
            new CommentNodeImpl(contents, _currentIndex++, _currentNode);
        }
    }

    ////////////////////////////////
    //
    // DTDHandler goodies
    public void unparsedEntityDecl(String name,
                                   String publicId,
                                   String systemId,
                                   String notationName)
    {
        _rootNode.unparsedEntityURITable.put(name, systemId);
    }
        
    public void notationDecl(String name,
                             String publicId,
                             String systemId) 
    {
    }
        

    /////////////

    public Node getRootNode()
    {
        return _rootNode;
    }
        
    private static boolean isWhitespace(char[] buf, int len) 
    {
        for (int i = 0; i < len; i++) {
            switch (buf[i]) {
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                break;
            default:
                return false;
            }
        }
        return true;
    }
        
    private final void flushData() 
    {
        if (_dataBufUsed > 0) {
            if (!isWhitespace(_dataBuf, _dataBufUsed) || 
                _currentNode.preserveSpace() || 
                ! _context.getStripSource(_currentNode.getName())) {

                new TextNodeImpl(_dataBuf, 0, _dataBufUsed,
                                 _currentIndex++, _currentNode);
            }
            _dataBufUsed = 0;
        }
    }

    ///////////////////////////////////////////////////////
    //
    // Private inner classes
    //

    // implements the Node interface in the "om" package
    static private abstract class NodeImpl implements Node 
    {
        ContainerNodeImpl parent;
        RootNodeImpl root;
        int index;  // an identifier based upon node count in document order?
        NodeImpl nextSibling;
        
        NodeImpl() 
        {
            this.index = 0;
            this.parent = null;
            this.nextSibling = null;
        }
        
        NodeImpl(int index, ContainerNodeImpl parent)
        {
            this.index = index;
            this.parent = parent;
            this.root = parent.root;
            this.nextSibling = null;
            if (parent.lastChild == null)
                parent.firstChild = parent.lastChild = this;
            else {
                parent.lastChild.nextSibling = this;
                parent.lastChild = this;
            }
        }

        public Node getParent() 
        {
            return parent;
        }

        public SafeNodeIterator getFollowingSiblings() 
        {
            return new NodeIteratorImpl(nextSibling);
        }

        /**
         * @return the base URI for this document (obtain from root?)
         */
        public URL getURL() 
        {
            return parent.getURL();
        }

        boolean canStrip()
        {
            return false;
        }

        public Node getAttribute(Name name) 
        {
            return null;
        }

        public String getAttributeValue(Name name) 
        {
            return null;
        }

        public SafeNodeIterator getAttributes()
        {
            return new NodeIteratorImpl(null);
        }

        public SafeNodeIterator getNamespaces()
        {
            return new NodeIteratorImpl(null);
        }

        public Name getName() 
        {
            return null;
        }

        public NamespacePrefixMap getNamespacePrefixMap() 
        {
            return parent.nsMap;
        }

        public int compareTo(Node node)
        {
            NodeImpl ni = (NodeImpl)node;
            if (root == ni.root) {
                return index - ((NodeImpl)node).index;
            }
            return root.compareRootTo(ni.root);
        }

        public Node getElementWithId(String name)
        {
            return root.getElementWithId(name);
        }

        public String getUnparsedEntityURI(String name)
        {
            return root.getUnparsedEntityURI(name);
        }

        public boolean isId(String name) 
        {
            return false;
        }

        public String getGeneratedId()
        {
            int d = root.getDocumentIndex();
            if (d == 0) {
                return "N" + String.valueOf(index);
            } else {
                return "N" + String.valueOf(d) + "_" + String.valueOf(index);
            }
        }

        public Node getRoot() {
            return root;
        }


        // javax.xml.trax.SourceLocator methods
        // FIXME: see if we can't get something useful from some DOMs
        public int getLineNumber() 
        {
            return parent.getLineNumber();
        }

        public int getColumnNumber()
        { return -1; }
        
        public String getSystemId()
        { 
            // CHECKME: is this the right meaning?
            return getURL().toString();
        }
        
        public String getPublicId()
        { return null; }
        

    }

    static private class NodeIteratorImpl implements SafeNodeIterator 
    {
        private NodeImpl nextNode;
        
        NodeIteratorImpl(NodeImpl nextNode) 
        {
            this.nextNode = nextNode;
        }
        
        public Node next() 
        {
            NodeImpl tem = nextNode;
            if (tem != null) {
                // linked list
                nextNode = tem.nextSibling;
            }
            return tem;
        }
    }

    static private class NamespaceNodeIterator implements SafeNodeIterator
    {
        private NamespacePrefixMap _namespaces;
        private int _index = 0;
        private ContainerNodeImpl _parent;

        NamespaceNodeIterator (NamespacePrefixMap map, ContainerNodeImpl parent)
        {
            _namespaces = map;
            _parent = parent;
            _index = map.getSize();
        }

        public Node next() 
        {
            Node ns = null;
            if (_index > 0) {
                --_index;
                ns = new NamespaceNode(_namespaces.getPrefix(_index), 
                                       _namespaces.getNamespace(_index),
                                       _parent);

            }
            return ns;
        }
    }


    static private class NamespaceNode extends NodeImpl
    {

        String _prefix;
        String _uri;

        public byte getType()
        {
            return Node.NAMESPACE;
        }

        NamespaceNode(String prefix, String uri, ContainerNodeImpl parent)
        {
            _prefix = prefix;
            _uri = uri;
            this.parent = parent;
        }

        public Name getName()
        { 
            return new Name() {
                    public String getNamespace() { return null; }
                    public String getLocalPart() {  return _prefix; }
                    public String getPrefix() {  return null; }
                    public Object getCreator() {  return null; }
                    public String toString() { return _prefix; }
                };
        }

        public String getData()
        {
            return _uri;
        }


        public SafeNodeIterator getChildren() 
        {
            return new NodeIteratorImpl(null);
        }


    }

    
    // for Elements and the RootNode
    static private abstract class ContainerNodeImpl extends NodeImpl 
    {
        NodeImpl firstChild;
        NodeImpl lastChild;
        NamespacePrefixMap nsMap;
        
        ContainerNodeImpl(NamespacePrefixMap nsMap) 
        {
            this.nsMap = nsMap;
        }
        
        ContainerNodeImpl(int index, ContainerNodeImpl parent) 
        {
            super(index, parent);
            nsMap = parent.nsMap;
        }
        
        public SafeNodeIterator getChildren()
        {
            return new NodeIteratorImpl(firstChild);
        }
        
        public String getData() 
        {
            return null;
        }

        boolean preserveSpace() 
        {
            return false;
        }

        public NamespacePrefixMap getNamespacePrefixMap() 
        {
            return nsMap;
        }

        void addId(String id, NodeImpl node)
        {
            parent.addId(id, node);
        }
    }

    static private class RootNodeImpl extends ContainerNodeImpl 
    {
        private String systemId;
        private int documentIndex;
        private Hashtable idTable = new Hashtable();
        private Hashtable unparsedEntityURITable = new Hashtable();
        
        RootNodeImpl(String systemId, int documentIndex,
                     NamespacePrefixMap nsMap) 
        {
            super(nsMap);
            this.systemId = systemId;
            this.documentIndex = documentIndex;
            this.root = this;
        }

        public byte getType() 
        {
            return Node.ROOT;
        }

        /**
         * @return the base URI for this document if systemId was set, otherwise, null
         */
        public URL getURL() 
        {
            if (systemId != null) {
                try {
                    return new URL(systemId);
                }
                catch (MalformedURLException e) { }
            }
            return null;
        }

        /**
         * allow fixup of SystemID after parsing begins
         */
        public void setSystemId(String sysId) 
        {
            systemId = sysId;
        }

        public int getLineNumber() 
        {
            return 1;
        }
        
        public Node getElementWithId(String name) 
        {
            return (Node)idTable.get(name);
        }
        
        /**
         * hash an Element Node which has an ID type attribute
         */
        void addId(String id, NodeImpl node) 
        {
            if (idTable.get(id) == null) {
                idTable.put(id, node);
            }
        }
        
        public String getUnparsedEntityURI(String name) 
        {
            return (String)unparsedEntityURITable.get(name);
        }
        
        int compareRootTo(RootNodeImpl r) 
        {
            if (systemId == null) {
                if (r.systemId == null) {
                    return documentIndex - r.documentIndex;
                }
                return -1;
            }
            else if (r.systemId == null)
                return 1;

            int n = systemId.compareTo(r.systemId);
            if (n != 0) {
                return n;
            }
            return documentIndex - r.documentIndex;
        }

        int getDocumentIndex() 
        {
            return documentIndex;
        }
        
    }
    
    static private class NullLocator implements Locator 
    {
        public String getPublicId() { return null; }
        public String getSystemId() { return null; }
        public int getLineNumber() { return -1; }
        public int getColumnNumber() { return -1; }
    }
    
    static private class ElementNodeImpl extends ContainerNodeImpl 
    {
        private Name name;
        private Object[] atts;
        private int lineNumber;
        private String systemId;
        
        ElementNodeImpl(String namespaceURI, String name, Attributes attList, Locator loc,
                        int index, ContainerNodeImpl parent, Hashtable namespaces) throws XSLException 
        {
            super(index, parent);
            lineNumber = loc.getLineNumber();
            systemId = loc.getSystemId();
            int nAtts = attList.getLength();

            if (namespaces.size() > 0) {
                Enumeration keys = namespaces.keys();
                while (keys.hasMoreElements()) {
                    String prefix = (String) keys.nextElement();
                    String ns =  (String) namespaces.get(prefix);
                    
                    //    System.out.println("SAXTwoOMBuilder:ElementNodeImpl()  -- binding prefix [" + prefix + "] to [" + ns  + "]");
                    
                    // unsetting default to "no" namespace?
                    if (prefix.length() == 0) {
                        if (ns.length() == 0) {
                            nsMap = nsMap.unbindDefault();
                        } else {
                            nsMap = nsMap.bindDefault(ns);
                        }
                    } else {
                        if ("xmlns".equals(prefix)) {
                            throw new XSLException("atttempting to bind xmlns");
                        } 
                        nsMap = nsMap.bind(prefix, (String) namespaces.get(prefix));
                    }
                }
            }

            if (nAtts > 0) {

                //                 // first, we do namespace processing
                int nNsAtts = 0;
                for (int i = 0; i < nAtts; i++) {
                    String tem = attList.getQName(i);

                    if (tem.startsWith("xmlns")) {
                        nNsAtts++;

                        // default namespace? (no colon)
                        if (tem.length() == 5) {
                            String ns = attList.getValue(i);

                            // unsetting default to "no" namespace?
                            if (ns.length() == 0) {
                                nsMap = nsMap.unbindDefault();
                            } else {
                                nsMap = nsMap.bindDefault(ns);
                            }
                        } else if (tem.charAt(5) == ':') {
                            if ("xmlns".equals(tem.substring(6))) {
                                throw new XSLException("atttempting to bind xmlns 2");
                            } 

                            nsMap = nsMap.bind(tem.substring(6),
                                               attList.getValue(i));
                        }
                    }
                }

                // now, pick up the rest of the attributes
                int n = nAtts - nNsAtts;
                if (n > 0) {
                    Object[] vec = new Object[n * 2];
                    int j = 0;
                    for (int i = 0; i < nAtts; i++) {
                        String tem = attList.getQName(i);
                        if (!tem.startsWith("xmlns")) { 
                            // drop namespace declarations
                            
                            
                            // if the reader wasn't sending start/end mapping events, and we didn't get
                            //  namespace declarations, we'll still catch namespaces here
                            String namespace = attList.getURI(i);
                            int pix = tem.indexOf(':');
                            if (pix == -1) {
                                if (namespace.length() > 0) {
                                    nsMap = nsMap.bindDefault(namespace);
                                }
                            } else if (pix == 3 && tem.regionMatches(0, "xml", 0, 3)) {
                                // nothing, it's in XML namespace
                            } else {
                                nsMap = nsMap.bind(tem.substring(0, pix), namespace );
                            }
                            

                            vec[j++] = nsMap.expandAttributeName(tem, this);
                            // this next comment is some J.Clark history
                            // FIXME resolve relative URL
                            vec[j++] = attList.getValue(i);
                        }
                        // "ID" attribute type has two char type identifier 
                        if (attList.getType(i) != null && attList.getType(i).length() == 2) {
                            parent.addId(attList.getValue(i), this);
                        }
                    }
                    // Assign here to avoid inconsistent state if exception
                    // is thrown.
                    atts = vec;
                }
            }

            // again we ensure a mappnig has been provided
            int pix = name.indexOf(':');
            if (pix == -1) {
                if (namespaceURI != null && namespaceURI.length() > 0) {
                    nsMap = nsMap.bindDefault(namespaceURI);
                }
            } else if (pix == 3 && name.regionMatches(0, "xml", 0, 3)) {
                // nothing, it's in XML namespace
            } else {
                nsMap = nsMap.bind(name.substring(0, pix), namespaceURI );
            }
            
            this.name = nsMap.expandElementTypeName(name, this);
        }
        
        public Name getName() 
        {
            return name;
        }
        
        public byte getType()
        {
            return Node.ELEMENT;
        }

        public SafeNodeIterator getAttributes() 
        {
            return new SafeNodeIterator() {
                    private int i = 0;
                    public Node next() {
                        if (atts == null)
                            return null;
                        int i2 = i*2;
                        if (i2 == atts.length)
                            return null;
                        return new AttributeNodeImpl(index + ++i,
                                                     ElementNodeImpl.this,
                                                     (Name)atts[i2],
                                                     (String)atts[i2 + 1]);
                    }
                };
        }



        public SafeNodeIterator getNamespaces()
        {
            return new NamespaceNodeIterator(getNamespacePrefixMap(), this);
        }

        
        public Node getAttribute(Name name) 
        {
            if (atts != null) {
                for (int i = 0; i < atts.length; i += 2)
                    if (atts[i].equals(name))
                        return new AttributeNodeImpl(this.index + (i >> 1) + 1,
                                                     this,
                                                     name,
                                                     (String)atts[i + 1]);
            }
            return null;
        }
        
        public String getAttributeValue(Name name) 
        {
            if (atts != null) {
                for (int i = 0; i < atts.length; i += 2)
                    if (atts[i].equals(name))
                        return (String)atts[i + 1];
            }
            return null;
        }
        
        public int getLineNumber() 
        {
            return lineNumber;
        }
        
        //          public URL getURL() 
        //          {
        //              // Why me?
        //              if (systemId != null) {
        //                  try {
        //                      return new URL(systemId);
        //                  }
        //                  catch (MalformedURLException e) { }
        //              }
        //              return null;
        //          }
        
        public boolean isId(String name) 
        {
            return this.equals(getElementWithId(name));
        }
    }
    
    static private class PreserveElementNodeImpl extends ElementNodeImpl 
    {
        PreserveElementNodeImpl(String namespaceURI, String name, Attributes atts, Locator loc,
                                int index, ContainerNodeImpl parent, Hashtable namespaces)  
            throws XSLException 
        {
            super(namespaceURI, name, atts, loc, index, parent, namespaces);
        }

        boolean preserveSpace() 
        {
            return true;
        }
    }
    
    static private class AttributeNodeImpl extends NodeImpl 
    {
        private Name name;
        private String value;
        AttributeNodeImpl(int index, ContainerNodeImpl parent,
                          Name name, String value)
        {
            // Don't use super(parent) because it would add
            //   this node to the children.
            this.index = index;
            this.parent = parent;
            this.root = parent.root;
            this.name = name;
            this.value = value;
        }

        public byte getType() 
        {
            return Node.ATTRIBUTE;
        }

        public String getData() 
        {
            return value;
        }

        public Name getName() 
        {
            return this.name;
        }

        public SafeNodeIterator getChildren() 
        {
            return new NodeIteratorImpl(null);
        }

        public int hashCode() 
        {
            return index;
        }

        public boolean equals(Object obj) 
        {
            return (obj != null
                    && obj instanceof AttributeNodeImpl
                    && ((AttributeNodeImpl)obj).index == index);
        }
    }
    
    static private class TextNodeImpl extends NodeImpl
    {
        private String data;
     
        public TextNodeImpl(char[] buf, int off, int len,
                            int index, ContainerNodeImpl parent)
        {
            super(index, parent);
            data = new String(buf, off, len);
        }
        public byte getType() {
            return Node.TEXT;
        }
        public String getData() {
            return data;
        }
        public SafeNodeIterator getChildren() {
            return new NodeIteratorImpl(null);
        }
        
    }
    
    static private class StripTextNodeImpl extends TextNodeImpl 
    {
        public StripTextNodeImpl(char[] buf, int off, int len, int index, 
                                 ContainerNodeImpl parent) 
        {
            super(buf, off, len, index, parent);
        }
        boolean canStrip() {
            return true;
        }
    }

    static private class CommentNodeImpl extends NodeImpl 
    {
        private String data;
        
        public CommentNodeImpl(String data, int index, ContainerNodeImpl parent) {
            super(index, parent);
            this.data = data;
        }
        public byte getType() {
            return Node.COMMENT;
        }
        public String getData() {
            return data;
        }
        public SafeNodeIterator getChildren() {
            return new NodeIteratorImpl(null);
        }
        
    }
    
    static private class ProcessingInstructionNodeImpl extends NodeImpl
    {
        private Name name;
        private String data;
        private String systemId; // why does a PI need a sysID and a URL ?
        
        // FIXME should include location
        
        public ProcessingInstructionNodeImpl(String name, String data, 
                                             Locator loc, int index, 
                                             ContainerNodeImpl parent) 
        {
            super(index, parent);
            systemId = loc.getSystemId();
            this.name = parent.getNamespacePrefixMap().getNameTable().createName(name);
            this.data = data;
        }
        
        public Name getName() 
        {
            return name;
        }
        
        public byte getType()
        {
            return Node.PROCESSING_INSTRUCTION;
        }
        
        public String getData()
        {
            return data;
        }
        
        public SafeNodeIterator getChildren()
        {
            return new NodeIteratorImpl(null);
        }

        public URL getURL() 
        {
            if (systemId != null) {
                try {
                    return new URL(systemId);
                }
                catch (MalformedURLException e) { }
            }
            return null;
        }
        
    }
    
}
