// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.NodeSetExpr;

/**
 * apply-templates!
 */
class ProcessAction extends ParamAction
{
    private NodeSetExpr expr;
    private Name modeName;
    
    ProcessAction(NodeSetExpr expr, Name modeName)
    {
        this.expr = expr;
        this.modeName = modeName;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result)
        throws XSLException
    {
        context.process(expr.eval(sourceNode, context),
                        modeName,
                        getParamNames(),
                        getParamValues(sourceNode, context),
                        result);
    }
}
