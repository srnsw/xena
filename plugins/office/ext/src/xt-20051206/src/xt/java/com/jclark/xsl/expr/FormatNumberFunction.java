// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import java.text.DecimalFormat;

class FormatNumberFunction extends Function2 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2) 
    {
        final NumberExpr ne = e1.makeNumberExpr();
        final StringExpr se = e2.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) 
		    throws XSLException 
		{
                    try {
                        return new DecimalFormat(se.eval(node, 
							 context)).format(ne.eval(node,
										  context));
                    }
                    catch (IllegalArgumentException e) {
                        throw new XSLException("invalid format pattern");
                    }
                }
            };
    }
}
