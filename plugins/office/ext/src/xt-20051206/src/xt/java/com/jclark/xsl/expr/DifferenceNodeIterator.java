// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class DifferenceNodeIterator implements NodeIterator 
{
    private NodeIterator iter1;
    private NodeIterator iter2;
    private Node node1;
    private Node node2;

    public DifferenceNodeIterator(NodeIterator iter1, NodeIterator iter2)
	throws XSLException 
    {
        this.iter1 = iter1;
        this.iter2 = iter2;
        this.node1 = iter1.next();
        this.node2 = iter2.next();
    }

    public Node next() throws XSLException 
    {
        while (node1 != null) {
            if (node2 == null) {
                Node tem = node1;
                node1 = iter1.next();
                return tem;
            }
            int cmp = node1.compareTo(node2);
            if (cmp < 0) {
                Node tem = node1;
                node1 = iter1.next();
                return tem;
            }
            if (cmp == 0) {
                node1 = iter1.next();
                node2 = iter2.next();
            }
            else
                node2 = iter2.next();
        }
        return null;
    }
}

