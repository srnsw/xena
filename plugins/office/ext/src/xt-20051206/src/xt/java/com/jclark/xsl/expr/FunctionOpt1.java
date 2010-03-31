// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;

abstract class FunctionOpt1 implements Function 
{
    abstract ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException;
    
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) throws ParseException {
        if (e.length > 1)
            throw new ParseException("expected zero or one argument");
        return makeCallExpr(e.length == 0 ? new SelfAxisExpr() : e[0]);
    }
}
