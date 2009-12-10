// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NamespaceAxisExpr extends AxisExpr
{
    public NodeIterator eval(Node node, ExprContext context)
    {
        return node.getNamespaces();
    }

    int getOptimizeFlags()
    {
        return STAYS_IN_SUBTREE | SINGLE_LEVEL;
    }
    /* OPT: Implement compose for when the expr is a ParentAxis */
}
