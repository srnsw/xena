// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class FilterExpr extends ConvertibleNodeSetExpr 
{
    private final ConvertibleNodeSetExpr expr;
    private final BooleanExpr predicate;

    FilterExpr(ConvertibleNodeSetExpr expr, BooleanExpr predicate) 
    {
        this.expr = expr;
        this.predicate = predicate;
    }

    public NodeIterator eval(Node node, ExprContext context) throws XSLException 
    {
        return new FilterNodeIterator(expr.eval(node, context),
                                      context,
                                      predicate);
    }

    /* OPT: if the expr is of the form position()=n,
       then SINGLE_LEVEL must be true */
    int getOptimizeFlags() 
    {
        return expr.getOptimizeFlags();
    }
}
