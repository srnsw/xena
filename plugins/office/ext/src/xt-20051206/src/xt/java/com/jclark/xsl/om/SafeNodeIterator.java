// $Id$

package com.jclark.xsl.om;

/**
 * represents a list of Nodes -- the next() function promises to not throw an exception
 */
public interface SafeNodeIterator extends NodeIterator 
{
    /**
     * get the next Node in the list, and don't throw any Exception
     */
    Node next();
}
