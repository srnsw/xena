// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;

/**
 * base class for all functions taking no arguments
 */
abstract class Function0 implements Function
{
    
    abstract ConvertibleExpr makeCallExpr() 
        throws ParseException;
    
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) 
	throws ParseException 
    {
        if (e.length != 0) {
            throw new ParseException("expected zero arguments");
	}
        return makeCallExpr();
    }
}
