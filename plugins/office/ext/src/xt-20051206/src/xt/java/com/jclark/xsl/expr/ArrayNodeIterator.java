// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class ArrayNodeIterator implements NodeIterator
{
    private int i;
    private int len;
    private Node[] nodes;


    /**
     *
     */
    ArrayNodeIterator(Node[] nodes, int start, int end)
    {
        this.nodes = nodes;
        this.len = end;
        this.i = start;
    }

    public Node next()
    {
        if (i == len) {
            return null;
	}
        return nodes[i++];
    }
}
