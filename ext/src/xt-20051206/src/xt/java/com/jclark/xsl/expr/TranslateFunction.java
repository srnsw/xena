// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class TranslateFunction extends Function3 {
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2, ConvertibleExpr e3) {
        final StringExpr se1 = e1.makeStringExpr();
        final StringExpr se2 = e2.makeStringExpr();
        final StringExpr se3 = e3.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return translate(se1.eval(node, context),
                                     se2.eval(node, context),
                                     se3.eval(node, context));
                }
            };
    }

    private static String translate(String s1, String s2, String s3) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s1.length(); i++) {
            char c = s1.charAt(i);
            // FIXME deal with surrogates properly
            int j = s2.indexOf(c);
            if (j < s3.length())
                buf.append(j < 0 ? c : s3.charAt(j));
        }
        return buf.toString();
    }
}
