// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * xsl:value-of
 */
class ValueOfAction implements Action
{
    private StringExpr expr;
    private String attribute;
    
    ValueOfAction(StringExpr expr)
    {
        this.expr = expr;

        // what's this??
        this.attribute = attribute;
    }

    /**
     *
     */    
    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) 
        throws XSLException
    {
        result.characters(expr.eval(sourceNode, context));
    }

}
