// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

/**
 * represents an XML Element's Attribute constructed around
 * a W3C level 1 DOM Attribute
 */
class AttributeNode extends NodeBase
{
    final Name name;
    String value;
    AttributeNode(Name name,
                  org.w3c.dom.Node domNode,
                  ContainerNode parent,
                  int childIndex)
    {
        super(domNode, parent, childIndex);
        this.name = name;
    }

    public byte getType()
    {
        return ATTRIBUTE;
    }

    public Name getName()
    {
        return name;
    }
  
    public final String getData()
    {
        if (value == null) {
            value = domNode.getNodeValue();
        }
        return value;
    }

    public SafeNodeIterator getFollowingSiblings()
    {
        return NullNodeIterator.getInstance();
    }
}

