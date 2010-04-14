// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;

/**
 * binds a local parameter to a name
 * <xsl:param />
 */
class BindLocalParamAction implements Action 
{
    private final Name name;
    private final VariantExpr expr;

    BindLocalParamAction(Name name, VariantExpr expr)
    {
        this.name = name;
        this.expr = expr;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) throws XSLException
    {
        Variant value = context.getParam(name); 

        // was it passed in the context?
        if (value == null) {
            // no, use the default value
            value = expr.eval(sourceNode, context);
        }
        context.bindLocalVariable(name, value);
    }
}
