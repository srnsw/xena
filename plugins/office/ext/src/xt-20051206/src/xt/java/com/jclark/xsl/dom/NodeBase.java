// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;
import java.net.URL;

/**
 *  Base class wrapper which presents  our xt om Node 
 * interface around a W3C Level 1 DOM node
 */
abstract class NodeBase implements Node
{
    final org.w3c.dom.Node domNode;
    int level;
    private int childIndex;
    ContainerNode parent;
    RootNode root;

    NodeBase(org.w3c.dom.Node domNode)
    {
        this.domNode = domNode;
        childIndex = 0;
        level = 0;
    }

    NodeBase(org.w3c.dom.Node domNode, 
             ContainerNode parent, 
             int childIndex)
    {
        this.domNode = domNode;
        this.parent = parent;
        this.root = parent.root;
        this.childIndex = childIndex;
        this.level = parent.level + 1;
    }

    public Node getParent()
    {
        return parent;
    }

    public String getGeneratedId()
    {
        return parent.getGeneratedId() + "." + Integer.toString(childIndex);
    }

    public int compareTo(Node node)
    {
        NodeBase other = (NodeBase)node;
        if (root == other.root)
            return compare(this, other);
        return root.compareRootTo(other.root);
    }

    private static final int compare(NodeBase node1, NodeBase node2)
    {
        int level1 = node1.level;
        int level2 = node2.level;
        int levelDiff = level1 - level2;
        while (level1 > level2) {
            node1 = node1.parent;
            level1--;
        }
        while (level2 > level1) {
            node2 = node2.parent;
            level2--;
        }
        int ret = compareSameLevel(node1, node2);
        if (ret != 0) {
            return ret;
	}
        return levelDiff;
    }

    private static final int compareSameLevel(NodeBase node1, NodeBase node2)
    {
        if (node1.level == 0)
            return 0;
        if (node1.domNode.equals(node2.domNode))
            return 0;
        int ret = compareSameLevel(node1.parent, node2.parent);
        if (ret != 0)
            return ret;
        return node1.childIndex - node2.childIndex;
    }

    /**
     * default behavior -- return null. Descendant classes override
     */
    public String getData()
    {
        return null;
    }

    /**
     * default behavior -- return null. Descendant classes override
     */
    public Name getName()
    {
        return null;
    }

    /**
     * default behavior -- return null. Descendant classes override
     */
    public String getAttributeValue(Name name)
    {
        return null;
    }

    /**
     * default behavior -- return null. Descendant classes override
     */
    public Node getAttribute(Name name)
    {
        return null;
    }

    public Node getElementWithId(String id)
    {
        return root.getElementWithId(id);
    }

    public boolean isId(String id)
    {
        return root.isId(domNode, id);
    }

    public NamespacePrefixMap getNamespacePrefixMap()
    {
        return parent.getNamespacePrefixMap();
    }

    public String getUnparsedEntityURI(String name)
    {
        return root.getUnparsedEntityURI(name);
    }

    public SafeNodeIterator getChildren()
    {
        return NullNodeIterator.getInstance();
    }

    public SafeNodeIterator getAttributes()
    {
        return NullNodeIterator.getInstance();
    }


    public SafeNodeIterator getNamespaces()
    {
        return NullNodeIterator.getInstance();
    }

    public URL getURL() 
    {
        return root.getURL();
    }

    // TrAX SourceLocater methods
    public String getSystemId()
    {
        // FIXME: do we really want the original string
        return getURL().toString();
    }

    public String getPublicId()
    {
        return null;
    }

    public int getLineNumber() 
    {
        return -1;
    }

    public int getColumnNumber() 
    {
        return -1;
    }

    public SafeNodeIterator getFollowingSiblings() 
    {
        SafeNodeIterator iter = new SiblingNodeIterator(parent, childIndex, 
                                                        domNode);
        iter.next();
        return iter;
    }

    public boolean equals(Object obj) 
    {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NodeBase)) {
            return false;
        }
        return ((NodeBase)obj).domNode.equals(domNode);
    }

    public Node getRoot() 
    {
        return root;
    }
}
