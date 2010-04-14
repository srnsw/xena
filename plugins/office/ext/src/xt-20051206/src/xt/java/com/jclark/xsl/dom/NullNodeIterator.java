// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

/**
 * provides a singleton Node iterator representing 0 Nodes
 */ 
class NullNodeIterator implements SafeNodeIterator
{
    private static NullNodeIterator theInstance = new NullNodeIterator();

    private NullNodeIterator()
    {
    }

    public static NullNodeIterator getInstance()
    {
        return theInstance;
    }

    /**
     * @return null
     */
    public final Node next() { return null; }
}
