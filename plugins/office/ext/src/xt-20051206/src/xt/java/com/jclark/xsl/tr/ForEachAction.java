// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.NodeSetExpr;

/**
 * <xsl:for-each
 */
class ForEachAction implements Action
{
    private NodeSetExpr expr;
    private Action action;

    ForEachAction(NodeSetExpr expr, Action action)
    {
        this.expr = expr;
        this.action = action;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) 
        throws XSLException
    {
        context.invoke(expr.eval(sourceNode, context), action, result);
    }
}

