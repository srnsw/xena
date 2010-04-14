// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;

/**
 * binds a variable to a name
 */
class BindLocalVariableAction implements Action
{
    private final Name name;
    private final VariantExpr expr;

    BindLocalVariableAction(Name name, VariantExpr expr)
    {
        this.name = name;
        this.expr = expr;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) throws XSLException
    {
        context.bindLocalVariable(name, expr.eval(sourceNode, context));
    }
}
