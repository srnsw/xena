// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NotFunction extends Function1 {
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) {
        final BooleanExpr be = e.makeBooleanExpr();
        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, ExprContext context) throws XSLException {
                    return !be.eval(node, context);
                }
            };
    }
}
