// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class SubstringAfterFunction extends Function2 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2) 
    {
        final StringExpr se1 = e1.makeStringExpr();
        final StringExpr se2 = e2.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return stringAfter(se1.eval(node, context),
                                       se2.eval(node, context));
                }
            };
    }

    static final String stringAfter(String s1, String s2) 
    {
        int i = s1.indexOf(s2);
        return i < 0 ? "" : s1.substring(i + s2.length());
    } 
}
