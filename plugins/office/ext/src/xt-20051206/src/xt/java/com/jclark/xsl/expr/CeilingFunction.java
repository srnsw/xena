// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * the XPath Function: number ceiling(number) 
 *
 *        The ceiling function returns the smallest
 *        (closest to negative infinity) number that is not less than the
 *        argument and that is an integer.
 */
class CeilingFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException 
    {

        final NumberExpr ne = e.makeNumberExpr();

        return new ConvertibleNumberExpr() 
            {
                // oddly uses a double instead of an int
                public double eval(Node node, ExprContext context) 
                    throws XSLException 
                {
                    return Math.ceil(ne.eval(node, context));
                }
            };
    }
}
