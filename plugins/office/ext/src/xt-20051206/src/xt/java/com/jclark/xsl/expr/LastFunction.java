// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class LastFunction extends Function0 {
    ConvertibleExpr makeCallExpr() {
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context) throws XSLException {
                    return context.getLastPosition();
                }
            };
    }
}
