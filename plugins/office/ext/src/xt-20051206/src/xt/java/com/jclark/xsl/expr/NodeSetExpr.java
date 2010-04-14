// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * A NodeSetExpr (compiled XPath), when evaluated with a contextNode
 * and an ExpressionContext, yields a NodeIterator
 */
public interface NodeSetExpr 
{
    /**
     * evaluate the expression with a contextNode
     * and ExprContext
     */
    NodeIterator eval(Node node, 
                      ExprContext context) 
        throws XSLException;
}
