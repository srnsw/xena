// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * An XPath expression component representing
 * the ancestor-or-self axis
 */
class AncestorOrSelfAxisExpr extends ReverseAxisExpr
{
    /**
     * When evaluated, returns a Node iterator for
     * the context node and its ancestors
     */
    public NodeIterator eval(Node node, ExprContext context)
    {
        return new AncestorsOrSelfNodeIterator(node);
    }
}
