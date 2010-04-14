// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class ParentAxisExpr extends AxisExpr {
    public NodeIterator eval(Node node, ExprContext context) {
        return new SingleNodeIterator(node.getParent());
    }
    int getOptimizeFlags() {
        return SINGLE_LEVEL;
    }
}
