// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class FalseExpr extends ConvertibleBooleanExpr 
{
    public boolean eval(Node node, ExprContext context) 
    {
        return false;
    }
}
