// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.NumberExpr;
import com.jclark.xsl.conv.NumberListFormat;

/**
 *
 */
class ExprNumberAction implements Action
{
    private NumberExpr expr;
    private NumberListFormatTemplate formatTemplate;

    ExprNumberAction(NumberExpr expr, NumberListFormatTemplate formatTemplate)
    {
        this.expr = expr;
        this.formatTemplate = formatTemplate;
    }

    public void invoke(ProcessContext context, Node node, Result result) 
        throws XSLException
    {
        NumberListFormat format = formatTemplate.instantiate(context, node);
        result.characters(format.getPrefix(0));
        result.characters(format.formatNumber(0,
                                              Math.round((float)expr.eval(node, context))));
        result.characters(format.getSuffix());
    }
}
