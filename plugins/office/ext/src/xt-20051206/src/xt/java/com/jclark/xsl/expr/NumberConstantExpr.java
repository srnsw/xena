// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NumberConstantExpr extends ConvertibleNumberExpr {
    private final double number;

    NumberConstantExpr(double number) {
        this.number = number;
    }

    public double eval(Node node, ExprContext context) {
        return number;
    }
}
