// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NodeConstantExpr extends ConvertibleNodeSetExpr {
    private final Node node;

    NodeConstantExpr(Node node) {
        this.node = node;
    }

    public NodeIterator eval(Node contextNode, ExprContext context) {
        return new SingleNodeIterator(node);
    }
}
