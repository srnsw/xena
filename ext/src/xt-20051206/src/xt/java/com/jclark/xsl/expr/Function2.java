// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;

/**
 *
 */
abstract class Function2 implements Function 
{
    abstract ConvertibleExpr makeCallExpr(ConvertibleExpr e1, ConvertibleExpr e2) 
        throws ParseException;

    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) 
        throws ParseException 
    {
        if (e.length != 2) {
            throw new ParseException("expected two arguments");
        }
        return makeCallExpr(e[0], e[1]);
    }
}
