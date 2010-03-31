// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class UnparsedEntityURIFunction extends Function1 {
    ConvertibleExpr makeCallExpr(ConvertibleExpr expr) throws ParseException {
        final StringExpr se = expr.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    String uri = node.getUnparsedEntityURI(se.eval(node, context));
                    if (uri == null)
                        return "";
                    return uri;
                }
            };
    }
}
