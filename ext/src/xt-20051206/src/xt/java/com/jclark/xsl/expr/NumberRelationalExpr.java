// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NumberRelationalExpr extends ConvertibleBooleanExpr {
    private final Relation rel;
    private final NumberExpr expr1;
    private final NumberExpr expr2;

    NumberRelationalExpr(Relation rel, NumberExpr expr1, NumberExpr expr2) {
        this.rel = rel;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException {
        return rel.relate(expr1.eval(node, context),
                          expr2.eval(node, context));
    }
}
