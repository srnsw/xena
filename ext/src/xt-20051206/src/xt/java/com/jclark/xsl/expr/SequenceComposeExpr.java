// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * Composition when expr1 is SINGLE_LEVEL and expr2 is STAYS_IN_SUBTREE.
 */
class SequenceComposeExpr extends ConvertibleNodeSetExpr 
{
    private final ConvertibleNodeSetExpr expr1;
    private final ConvertibleNodeSetExpr expr2;
    
    SequenceComposeExpr(ConvertibleNodeSetExpr expr1, ConvertibleNodeSetExpr expr2)
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    public NodeIterator eval(Node node, ExprContext context) 
        throws XSLException 
    {
        return new SequenceComposeNodeIterator(expr1.eval(node, context),
                                               expr2,
                                               context);
    }

    
    int getOptimizeFlags() 
    {
        // if expr2 is SINGLE_LEVEL then this will be too
        // HST: but will only be STAYS_IN_SUBTREE if expr1 is also
        return expr2.getOptimizeFlags() & expr1.getOptimizeFlags();
    }
}
