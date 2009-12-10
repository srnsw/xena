// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class LocalVariableRefExpr extends ConvertibleVariantExpr 
{
    private final Name name;
    
    LocalVariableRefExpr(Name name) 
    {
        this.name = name;
    }
    
    public Variant eval(Node node, ExprContext context) 
        throws XSLException 
    {
        return context.getLocalVariableValue(name);
    }
}
