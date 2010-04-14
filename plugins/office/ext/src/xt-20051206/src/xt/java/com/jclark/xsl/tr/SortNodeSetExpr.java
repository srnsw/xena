// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.NodeSetExpr;
import com.jclark.xsl.expr.ExprContext;
import com.jclark.xsl.expr.NodeListSorter;

/**
 * sorts a node list before returning them from
 * a nested expression
 */
public class SortNodeSetExpr implements NodeSetExpr
{
    private NodeSetExpr expr;
    private ComparatorTemplate comparatorTemplate;

    public SortNodeSetExpr(NodeSetExpr expr,
                           ComparatorTemplate comparatorTemplate)
    {
        this.expr = expr;
        this.comparatorTemplate = comparatorTemplate;
    }

    public NodeIterator eval(Node node, ExprContext context) throws XSLException
    {
        try {
            return NodeListSorter.sort(expr.eval(node, context),
                                       comparatorTemplate.instantiate(node, context));
        }
        catch (UncheckedXSLException e) {
            throw e.getXSLException();
        }
    }

}
