// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class RoundFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException 
    {
        final NumberExpr ne = e.makeNumberExpr();
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context) 
		    throws XSLException 
		{
                    double n = ne.eval(node, context);
                    double r = Math.floor(n + 0.5);
                    if (r == 0.0 && n < 0.0) {
                        return -r; // round(-0.2) returns -0 not 0
		    }
                    return r;
                }
            };
    }
}
