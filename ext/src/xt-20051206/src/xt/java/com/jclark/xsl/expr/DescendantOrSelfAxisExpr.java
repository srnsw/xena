// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
/**
 * an XPath expression "//"
 */
class DescendantOrSelfAxisExpr extends AxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) 
    {
        return new DescendantsOrSelfNodeIterator(node);
    }

    int getOptimizeFlags() 
    {
        return STAYS_IN_SUBTREE;
    }

    ConvertibleNodeSetExpr compose(ConvertibleNodeSetExpr expr)
    {
        Pattern pattern = expr.getChildrenNodePattern();
        if (pattern != null) {
            return new NodeTestExpr(new DescendantAxisExpr(), pattern);
	}
        if (expr.getClass() == ChildAxisExpr.class) {
            return new DescendantAxisExpr();
	}
        if ((expr.getOptimizeFlags() & STAYS_IN_SUBTREE) != 0) {
            return new SubtreeExpr(expr);
	}
        return super.compose(expr);
    }
}
