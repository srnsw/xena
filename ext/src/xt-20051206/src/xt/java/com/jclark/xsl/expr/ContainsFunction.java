// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * Represents the XPath Function: boolean contains(string, string) 
 *
 *    The contains function returns true if the first argument 
 * string contains the second argument string, and
 *        otherwise returns false.
 */
class ContainsFunction extends Function2
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2)
    {
        final StringExpr se1 = e1.makeStringExpr();
        final StringExpr se2 = e2.makeStringExpr();

        return new ConvertibleBooleanExpr() 
            {
                public boolean eval(Node node, 
                                    ExprContext context) 
                    throws XSLException 
                {
                    return se1.eval(node, 
                                    context).indexOf(se2.eval(node, 
                                                              context)) >= 0;
                }
            };
    }
}
