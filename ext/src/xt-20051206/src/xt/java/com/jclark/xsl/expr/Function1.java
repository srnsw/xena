// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;

/**
 * base class for all functions taking one arguments
 */
abstract class Function1 implements Function 
{
    abstract ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException;

    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) 
	throws ParseException 
    {
        if (e.length != 1) {
            throw new ParseException("expected one argument");
	}
        return makeCallExpr(e[0]);
    }
}
