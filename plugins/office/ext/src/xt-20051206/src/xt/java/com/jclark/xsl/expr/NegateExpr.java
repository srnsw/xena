// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NegateExpr extends ConvertibleNumberExpr {
    private final NumberExpr expr;

    NegateExpr(NumberExpr expr) {
        this.expr = expr;
    }

    public double eval(Node node, ExprContext context) throws XSLException {
        return -expr.eval(node, context);
    }
}
