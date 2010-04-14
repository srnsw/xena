// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class VariantRelationalExpr extends ConvertibleBooleanExpr {
    private final Relation rel;
    private final VariantExpr expr1;
    private final VariantExpr expr2;

    VariantRelationalExpr(Relation rel, VariantExpr expr1, VariantExpr expr2) {
        this.rel = rel;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException {
        return rel.relate(expr1.eval(node, context),
                          expr2.eval(node, context));
    }
}
