// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;

/**
 * abstract base class for XPath functions which take 3 arguments
 */
abstract class Function3 implements Function 
{
    abstract ConvertibleExpr makeCallExpr(ConvertibleExpr e1, 
                                          ConvertibleExpr e2, 
                                          ConvertibleExpr e3) 
        throws ParseException;
    
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) 
        throws ParseException 
    {
        if (e.length != 3) {
            throw new ParseException("expected three arguments");
        }
        return makeCallExpr(e[0], e[1], e[2]);
    }
}
