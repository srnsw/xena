// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * an XPath expression component representing
 * an ancestor axis
 */
class AncestorAxisExpr extends ReverseAxisExpr
{
    /**
     * when evaluated, return a NodeIterator of parent and its 
     * ancestors
     */
    public NodeIterator eval(Node node, ExprContext context)
    {
        return new AncestorsOrSelfNodeIterator(node.getParent());
    }
}
