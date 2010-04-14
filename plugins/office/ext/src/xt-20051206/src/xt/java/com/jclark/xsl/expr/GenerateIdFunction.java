// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class GenerateIdFunction extends FunctionOpt1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr expr) throws ParseException 
    {
        final NodeSetExpr nse = expr.makeNodeSetExpr();

        return new ConvertibleStringExpr()  {
                public String eval(Node node, ExprContext context)
		    throws XSLException 
		{
                    node = nse.eval(node, context).next();
                    if (node != null) {
                        return node.getGeneratedId();
		    }
                    return "";
                }
            };
    }
}
