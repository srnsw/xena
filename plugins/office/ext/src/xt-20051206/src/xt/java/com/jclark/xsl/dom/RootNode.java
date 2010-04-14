// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;
import com.jclark.xsl.tr.LoadContext;
import java.net.URL;
import java.net.MalformedURLException;

class RootNode extends ContainerNode
{
    private org.w3c.dom.Document document;   // maybe null
    org.w3c.dom.Document ownerDocument; // never null
    private org.w3c.dom.NamedNodeMap entities;
    private DOMExtensions extend;
    NameTable nameTable;
    int documentIndex;
    URL baseURL;
    LoadContext loadContext;
    boolean includeComments;
    boolean includeProcessingInstructions;

    RootNode(org.w3c.dom.Node node,
             DOMExtensions extend,
             LoadContext loadContext,
             NameTable nameTable,
             String baseURL,
             int documentIndex)
    {
        super(node);
        this.extend = extend;
        this.nameTable = nameTable;
        this.parent = null;
        this.root = this;
        this.prefixMap = nameTable.getEmptyNamespacePrefixMap();
        this.loadContext = loadContext;
        includeProcessingInstructions = loadContext.getIncludeProcessingInstructions();
        includeComments = loadContext.getIncludeComments();
        try {
            this.baseURL = new URL(baseURL);
        }
        catch (MalformedURLException e) { }
        if (node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE) {
            this.document = (org.w3c.dom.Document)node;
            this.ownerDocument = this.document;
            org.w3c.dom.DocumentType doctype = document.getDoctype();
            if (doctype != null)
                entities = doctype.getEntities();
        }
        else
            this.ownerDocument = node.getOwnerDocument();
    }

    int compareRootTo(RootNode node)
    {
        return documentIndex - node.documentIndex;
    }

    public byte getType()
    {
        return ROOT;
    }
  
    public URL getURL()
    {
        return baseURL;
    }
  
    public String getUnparsedEntityURI(String name)
    {
        if (entities == null)
            return null;
        org.w3c.dom.Entity entity = (org.w3c.dom.Entity)entities.getNamedItem(name);
        if (entity == null || entity.getNotationName() == null)
            return null;
        String systemId = entity.getSystemId();
        try {
            return new URL(baseURL, systemId).toString();
        }
        catch (MalformedURLException e) {
            return systemId;
        }
    }

    boolean isId(org.w3c.dom.Node node, String id)
    {
        if (document == null)
            return false;
        return node.equals(extend.getElementById(document, id));
    }

    public Node getElementWithId(String id)
    {
        if (document == null)
            return null;
        org.w3c.dom.Node node = extend.getElementById(document, id);
        if (node == null)
            return null;
        return createElement(node);
    }

    private ContainerNode createElement(org.w3c.dom.Node node)
    {
        org.w3c.dom.Node domParent = node.getParentNode();
        while (domParent.getNodeType() == org.w3c.dom.Node.ENTITY_REFERENCE_NODE)
            domParent = domParent.getParentNode();
        ContainerNode tem;
        if (domParent.equals(domNode))
            tem = this;
        else
            tem = createElement(domParent);
        // OPT It would be better to compute the child index lazily.
        return new ElementNode(node, tem, SiblingNodeIterator.computeChildIndex(tem, node));
    }

    public String getGeneratedId()
    {
        return "N" + Integer.toString(documentIndex);
    }

    public SafeNodeIterator getFollowingSiblings()
    {
        return NullNodeIterator.getInstance();
    }

}
