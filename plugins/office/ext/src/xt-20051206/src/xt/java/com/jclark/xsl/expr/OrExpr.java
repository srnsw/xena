// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class OrExpr extends ConvertibleBooleanExpr {
    private final BooleanExpr expr1;
    private final BooleanExpr expr2;

    OrExpr(BooleanExpr expr1, BooleanExpr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException {
        return expr1.eval(node, context) || expr2.eval(node, context);
    }
}
