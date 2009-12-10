// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class FollowingSiblingAxisExpr extends AxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) {
        return node.getFollowingSiblings();
    }
    int getOptimizeFlags() {
        return SINGLE_LEVEL;
    }
}
