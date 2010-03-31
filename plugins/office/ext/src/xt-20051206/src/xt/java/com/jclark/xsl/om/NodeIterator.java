// $Id$

package com.jclark.xsl.om;

/**
 * represents a list of Nodes
 */
public interface NodeIterator 
{
    /**
     * return the next Node in the list
     */
    Node next() throws XSLException;
}
