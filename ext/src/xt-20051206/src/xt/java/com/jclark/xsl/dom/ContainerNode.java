// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

/**
 * presents a om.Node interface for a W3C DOM Node
 */
abstract class ContainerNode extends NodeBase
{
    NamespacePrefixMap prefixMap;
    boolean preserveSpace = false;

    ContainerNode(org.w3c.dom.Node domNode) {
        super(domNode);
    }

    ContainerNode(org.w3c.dom.Node domNode, ContainerNode parent, int childIndex)
    {
        super(domNode, parent, childIndex);
        prefixMap = parent.prefixMap;
        preserveSpace = parent.preserveSpace;
    }

    public boolean getPreserveSpace()
    {
        return preserveSpace;
    }

    public String getUnparsedEntityURI(String name)
    {
        return null;
    }

    public SafeNodeIterator getChildren()
    {
        org.w3c.dom.Node firstChild = domNode.getFirstChild();
        if (firstChild == null)
            return NullNodeIterator.getInstance();
        return new SiblingNodeIterator(this, 0, firstChild);
    }

    public final NamespacePrefixMap getNamespacePrefixMap()
    {
        return prefixMap;
    }
}

