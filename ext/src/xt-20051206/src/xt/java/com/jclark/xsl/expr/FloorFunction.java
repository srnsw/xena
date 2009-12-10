// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class FloorFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException
    {
        final NumberExpr ne = e.makeNumberExpr();
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context)
		    throws XSLException 
		{
                    return Math.floor(ne.eval(node, context));
                }
            };
    }
}
