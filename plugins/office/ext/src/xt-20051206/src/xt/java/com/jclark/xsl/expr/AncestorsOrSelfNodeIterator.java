// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * an Iterator that walks toward thwe document root
 */
public class AncestorsOrSelfNodeIterator implements NodeIterator
{
    private Node node;

    public AncestorsOrSelfNodeIterator(Node node)
    {
        this.node = node;
    }

    public Node next()
    {
        if (node == null) {
            return null;
	}
        Node tem = node;
        node = tem.getParent();
        return tem;
    }
}
