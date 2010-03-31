// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * XPath expression (component) representing descendant axis
 */
class DescendantAxisExpr extends AxisExpr 
{

    public NodeIterator eval(Node node, 
                             ExprContext context) throws XSLException 
    {
        NodeIterator iter = new DescendantsOrSelfNodeIterator(node);
        iter.next();  // skip self
        return iter;
    }

    int getOptimizeFlags() 
    {
        return STAYS_IN_SUBTREE;
    }
}
