// $Id$

package com.jclark.xsl.sax;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Hashtable;

import org.xml.sax.*;

import com.jclark.xsl.om.*;

import com.jclark.xsl.tr.Result;
import com.jclark.xsl.tr.LoadContext;

/**
 * parses a source doc or stylesheet into our own DOM like structure
 */
public class XMLProcessorImpl implements XMLProcessorEx
{
    
    // we expect to be able to re-use the parser for resolving
    // xsl:include and xsl:import

    private Parser parser;
    private ErrorHandler errorHandler;

    /**
     * construct with a SAX1 parser we may wish to re-use for
     * resolving e.g xsl:include
     */
    public XMLProcessorImpl(Parser parser) 
    {
        this.parser = parser;
    }
  
    /**
     *
     */
    public void setErrorHandler(ErrorHandler errorHandler) 
    {
        this.errorHandler = errorHandler;
    }

    /**
     * parse the xml stream at <code>source</code> building an
     * object model of all its nodes
     *
     * @return the document root
     */
    public Node load(InputSource source,
                     int documentIndex,
                     LoadContext context,
                     NameTable nameTable)
        throws IOException, XSLException 
    {

        try {
            // build a (xslt)dom with sax
            Builder builder = new BuilderImpl(context,
                                              source.getSystemId(),
                                              documentIndex,
                                              nameTable.getEmptyNamespacePrefixMap(),
                                              source);
            parser.setDocumentHandler(builder);
            parser.setDTDHandler(builder);
            parser.parse(source);           // build the object model
            return builder.getRootNode();
        }
        catch (SAXParseException e) {
            throw new XSLException(e);
        }
        catch (SAXException e) {
            Exception wrapped = e.getException();
            if (wrapped == null)
                throw new XSLException(e.getMessage());
            if (wrapped instanceof XSLException)
                throw (XSLException)e.getException();
            throw new XSLException(wrapped);
        }
    }

    /**
     * @return the document root
     */
    public Node load(URL url, 
                     int documentIndex,
                     LoadContext context, 
                     NameTable nameTable) 
        throws IOException, XSLException 
    {

        return load(new InputSource(url.toString()),
                    documentIndex,
                    context,
                    nameTable);
    }

    /**
     * 
     */
    static private Builder createBuilder(String systemId, 
                                         int documentIndex, LoadContext context,
                                         NameTable nameTable) 
    {
        return new BuilderImpl(context,
                               systemId,
                               documentIndex,
                               nameTable.getEmptyNamespacePrefixMap(),
                               null);
    }

    /**
     *
     */
    public Result createResult(Node baseNode,
                               int documentIndex,
                               LoadContext loadContext,
                               Node[] rootNode) throws XSLException 
    {

        URL baseURL = null;
        if (baseNode != null) {
            baseURL = baseNode.getURL();
        }
        String base;
        if (baseURL == null) {
            base = null;
        } else {
            base = baseURL.toString();
        }
        XMLProcessorImpl.Builder builder
            = XMLProcessorImpl.createBuilder(base,
                                             documentIndex,
                                             loadContext,
                                             baseNode.getNamespacePrefixMap().getNameTable());
        rootNode[0] = builder.getRootNode();
        return new MultiNamespaceResult(builder, errorHandler);
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
        public int getLineNumber() 
        {
            return parent.getLineNumber();
        }

        public int getColumnNumber()
        { return -1; }
        
        public String getSystemId()
        { 
            return getRoot().getSystemId();
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


    
    // for Elements and the RootNode, and ...
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
        
        RootNodeImpl(String systemId, int documentIndex, NamespacePrefixMap nsMap) {
            super(nsMap);
            this.systemId = systemId;
            this.documentIndex = documentIndex;
            this.root = this;
        }

        public byte getType() {
            return Node.ROOT;
        }

        /**
         * @return the base URI for this document if systemId was set, otherwise, null
         */
        public URL getURL() {
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
            if (idTable.get(id) == null)
                idTable.put(id, node);
        }
        
        public String getUnparsedEntityURI(String name) 
        {
            return (String)unparsedEntityURITable.get(name);
        }
        
        int compareRootTo(RootNodeImpl r) 
        {
            if (systemId == null) {
                if (r.systemId == null)
                    return documentIndex - r.documentIndex;
                return -1;
            }
            else if (r.systemId == null)
                return 1;

            int n = systemId.compareTo(r.systemId);
            if (n != 0)
                return n;
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
        
        ElementNodeImpl(String name, AttributeList attList, Locator loc,
                        int index, ContainerNodeImpl parent) throws XSLException 
        {
            super(index, parent);
            lineNumber = loc.getLineNumber();
            systemId = loc.getSystemId();
            int nAtts = attList.getLength();
            if (nAtts > 0) {

                // first, we do namespace processing
                int nNsAtts = 0;
                for (int i = 0; i < nAtts; i++) {
                    String tem = attList.getName(i);

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
                        String tem = attList.getName(i);
                        if (!tem.startsWith("xmlns")) {
                            vec[j++] = nsMap.expandAttributeName(tem, this);
                            // this next comment is some J.Clark history
                                // FIXME resolve relative URL
                            vec[j++] = attList.getValue(i);
                        }
                        if (attList.getType(i).length() == 2)
                            parent.addId(attList.getValue(i), this);
                    }
                    // Assign here to avoid inconsistent state if exception
                    // is thrown.
                    atts = vec;
                }
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


        public SafeNodeIterator getNamespaces()
        {
            return new NamespaceNodeIterator(getNamespacePrefixMap(), this);
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

        public String getSystemId()
        {
            return systemId;
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
        
        public boolean isId(String name) 
        {
            return this.equals(getElementWithId(name));
        }
    }
    
    static private class PreserveElementNodeImpl extends ElementNodeImpl 
    {
        PreserveElementNodeImpl(String name, AttributeList atts, Locator loc,
                                int index, ContainerNodeImpl parent)  
            throws XSLException 
        {
            super(name, atts, loc, index, parent);
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
            // Don't use super(parent) because it add's this node to the children.
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
        private String systemId; 
        
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

    // constructs an (xslt) dom 
    static interface Builder extends DocumentHandler, 
                                     CommentHandler, DTDHandler 
    {
        public Node getRootNode();
    }
    
    // constructs an (xslt) dom from SAX1 events
    static private class BuilderImpl implements Builder 
    {
        char[] dataBuf = new char[1024];
        int dataBufUsed = 0;
        RootNodeImpl rootNode;
        ContainerNodeImpl currentNode;
        int currentIndex = 1;
        boolean includeProcessingInstructions;
        boolean includeComments;
        LoadContext context;
        InputSource input;
        Locator locator = new NullLocator();
        
        BuilderImpl(LoadContext context, 
                    String systemId, 
                    int documentIndex,  
                    NamespacePrefixMap nsMap,
                    InputSource input) 
        {
            this.context = context;
            this.input = input;
            includeProcessingInstructions = 
                context.getIncludeProcessingInstructions();
            includeComments = context.getIncludeComments();

            currentNode = rootNode = new RootNodeImpl(systemId,
                                                      documentIndex,
                                                      nsMap);
        }
        
        public void startDocument() 
        { 
            if (rootNode.getURL() == null && input != null) {
                String newSysId = input.getSystemId();

                rootNode.setSystemId(newSysId);
            }
        }
        
        public void endDocument() 
        { }
        
        public void setDocumentLocator(Locator locator) 
        {
            this.locator = locator;
        }
        
        public void startElement(String name, AttributeList atts) throws SAXException
        {
            flushData();
            ElementNodeImpl element;
            boolean preserve;
            
            String space = atts.getValue("xml:space");

            String havelocator = (locator == null) ? "n" : "y";

            if (space == null)
                preserve = currentNode.preserveSpace();
            else if (space.equals("default"))
                preserve = false;
            else if (space.equals("preserve"))
                preserve = true;
            else
                preserve = currentNode.preserveSpace();
            try {
                if (preserve)
                    element = new PreserveElementNodeImpl(name, atts, locator,
                                                          currentIndex++, currentNode);
                else
                    element = new ElementNodeImpl(name, atts, locator,
                                                  currentIndex++, currentNode);
            }
            catch (XSLException e) {
                throw new SAXException(e);
            }
            currentIndex += atts.getLength();
            currentNode = element;
        }
        
        public void characters(char ch[], int start, int length)
        {
            int need = length + dataBufUsed;
            if (need > dataBuf.length) {
                int newLength = dataBuf.length << 1;
                while (need > newLength)
                    newLength <<= 1;
                char[] tem = dataBuf;
                dataBuf = new char[newLength];
                if (dataBufUsed > 0)
                    System.arraycopy(tem, 0, dataBuf, 0, dataBufUsed);
            }
            for (; length > 0; length--)
                dataBuf[dataBufUsed++] = ch[start++];
        }
        
        public void ignorableWhitespace(char ch[], int start, int length)
        {
            if (dataBufUsed > 0
                || currentNode.preserveSpace()
                || !context.getStripSource(currentNode.getName()))
                characters(ch, start, length);
        }
        
        public void endElement(String name)
        {
            flushData();
            currentNode = currentNode.parent;
        }
        
        public void processingInstruction(String target, String data) 
        {
            if (target == null)
                comment(data);
            else {
                if (includeProcessingInstructions) {
                    flushData();
                    new ProcessingInstructionNodeImpl(target, data, locator,
                                                      currentIndex++, currentNode);
                }
            }
        }
        
        public void comment(String contents)
        {
            if (includeComments) {
                flushData();
                new CommentNodeImpl(contents, currentIndex++, currentNode);
            }
        }
        
        public void unparsedEntityDecl(String name,
                                       String publicId,
                                       String systemId,
                                       String notationName)
        {
            rootNode.unparsedEntityURITable.put(name, systemId);
        }
        
        public void notationDecl(String name,
                                 String publicId,
                                 String systemId) 
        {
        }
        
        public Node getRootNode()
        {
            return rootNode;
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
            if (dataBufUsed > 0) {
                if (!isWhitespace(dataBuf, dataBufUsed)
                    || currentNode.preserveSpace()
                    || !context.getStripSource(currentNode.getName()))
                    new TextNodeImpl(dataBuf, 0, dataBufUsed, currentIndex++, currentNode);
                dataBufUsed = 0;
            }
        }
    }

}
