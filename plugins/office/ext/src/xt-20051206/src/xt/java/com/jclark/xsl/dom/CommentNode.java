// $Id$

package com.jclark.xsl.dom;

 import com.jclark.xsl.om.*;

/**
 *  Wraps a W3C DOM XML Comment Node as an om.Node
 */
class CommentNode extends NodeBase
{
    CommentNode(org.w3c.dom.Node domNode,
                ContainerNode parent,
                int childIndex)
    {
        super(domNode, parent, childIndex);
    }

    public byte getType()
    {
        return COMMENT;
    }

    public final String getData()
    {
        return domNode.getNodeValue();
    }
}
