// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;

/**
 *
 */
class UnbindLocalVariablesAction implements Action
{
    private final int n;

    UnbindLocalVariablesAction(int n)
    {
        this.n = n;
    }

    public void invoke(ProcessContext context, 
                       Node sourceNode, 
                       Result result)
    {
        context.unbindLocalVariables(n);
    }
}
