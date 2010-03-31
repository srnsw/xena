// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class ChildAxisExpr extends AxisExpr
{
    public NodeIterator eval(Node node, ExprContext context)
    {
        return node.getChildren();
    }

    int getOptimizeFlags()
    {
        return STAYS_IN_SUBTREE | SINGLE_LEVEL;
    }
}
