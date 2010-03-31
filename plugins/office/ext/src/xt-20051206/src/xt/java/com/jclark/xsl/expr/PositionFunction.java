// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * implements the XPath position() function, which
 * evaluates to the current node's position in the current node set
 */
class PositionFunction extends Function0 
{
    ConvertibleExpr makeCallExpr() 
    {
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context) 
		    throws XSLException 
		{
                    return context.getPosition();
                }
            };
    }
}
