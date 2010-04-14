// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * <xsl:processing-instruction
 */
class ProcessingInstructionAction implements Action
{
    private StringExpr nameExpr;
    private Action content;

    ProcessingInstructionAction(StringExpr nameExpr, Action content)
    {
        this.nameExpr = nameExpr;
        this.content = content;
    }

    public void invoke(ProcessContext context, Node sourceNode,
                       Result result) 
        throws XSLException
    {
        String name = nameExpr.eval(sourceNode, context);
        StringResult s = new StringResult(result);
        content.invoke(context, sourceNode, s);
        result.processingInstruction(name, s.toString());
    }
}
