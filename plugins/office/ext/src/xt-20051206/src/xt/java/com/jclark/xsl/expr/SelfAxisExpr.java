// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class SelfAxisExpr extends AxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) 
    {
        return new SingleNodeIterator(node);
    }

    int getOptimizeFlags() 
    {
        return STAYS_IN_SUBTREE|SINGLE_LEVEL;
    }

    ConvertibleNodeSetExpr compose(ConvertibleNodeSetExpr expr) 
    {
        return expr;
    }
}
