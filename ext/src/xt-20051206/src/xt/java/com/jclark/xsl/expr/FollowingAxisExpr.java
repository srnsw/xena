// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;
import com.jclark.xsl.om.NodeIterator;

/**
 *
 */
class FollowingAxisExpr extends AxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) 
    {
        return new FollowingNodeIterator(node);
    }
}
