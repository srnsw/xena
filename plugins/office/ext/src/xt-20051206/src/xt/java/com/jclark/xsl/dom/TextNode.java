// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

class TextNode extends NodeBase 
{
    StringBuffer buf;
    String value;
    TextNode(org.w3c.dom.Node domNode,
             ContainerNode parent,
             int childIndex)
    {
        super(domNode, parent, childIndex);
    }

    public final byte getType()
    {
        return TEXT;
    }

    void merge(String value)
    {
        if (buf == null)
            buf = new StringBuffer(domNode.getNodeValue());
        buf.append(value);
    }

    public final String getData()
    {
        if (value == null) {
            if (buf != null)
                value = buf.toString();
            else
                value = domNode.getNodeValue();
        }
        return value;
    }
}
