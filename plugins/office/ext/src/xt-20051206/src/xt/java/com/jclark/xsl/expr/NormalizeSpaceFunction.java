// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import java.util.StringTokenizer;

class NormalizeSpaceFunction extends FunctionOpt1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr expr) throws ParseException 
    {
        final StringExpr se = expr.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return normalize(se.eval(node, context));
                }
            };
    }

    /**
     *
     */
    private static String normalize(String s) {
        StringBuffer buf = new StringBuffer();
        for (StringTokenizer e = new StringTokenizer(s); e.hasMoreElements();) {
            if (buf.length() > 0)
                buf.append(' ');
            buf.append((String)e.nextElement());
        }
        return buf.toString();
    }
}
