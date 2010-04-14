// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class LiteralExpr extends ConvertibleStringExpr {
    private final String literal;

    LiteralExpr(String literal) {
        this.literal = literal;
    }

    public String eval(Node node, ExprContext context) {
        return literal;
    }

    public String constantValue() {
        return literal;
    }
}
