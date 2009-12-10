// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * an XPATH (XSLT) match pattern
 */
public interface Pattern 
{
    /**
     * return true if the given node matches this pattern
     * when evaluated in the given ExpressionContext
     */
    boolean matches(Node node, ExprContext context) 
        throws XSLException;
}
