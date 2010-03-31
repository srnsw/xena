// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class SubstringBeforeFunction extends Function2 {
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2) {
        final StringExpr se1 = e1.makeStringExpr();
        final StringExpr se2 = e2.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return stringBefore(se1.eval(node, context),
                                        se2.eval(node, context));
                }
            };
    }

    static final String stringBefore(String s1, String s2) {
        int i = s1.indexOf(s2);
        return i < 0 ? "" : s1.substring(0, i);
    } 
}
