// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.om.Node;

/**
 *
 */
public interface ExtensionContext 
{
    /**
     *
     */
    boolean available(String name);

    /**
     *
     */
    Object call(String name, Node currentNode, Object[] args) 
        throws XSLException;
}
