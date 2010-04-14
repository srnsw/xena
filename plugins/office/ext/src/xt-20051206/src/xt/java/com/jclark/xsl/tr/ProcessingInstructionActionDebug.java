// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * <xsl:processing-instruction
 */
class ProcessingInstructionActionDebug extends ProcessingInstructionAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;


    ProcessingInstructionActionDebug(
                                     ActionDebugTarget target, 
                                     Node sheetNode,
                                     String templateIDHook,
                                     StringExpr nameExpr, Action content)
    {
        super(nameExpr, content);

        _target = target;
        _sheetNode = sheetNode;
        _templateIDHook = templateIDHook;

    }


    /**
     *
     */    
    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) 
        throws XSLException
    {
        _target.startAction(_sheetNode, sourceNode, this);

        super.invoke(context, sourceNode, result);

        _target.endAction(_sheetNode, sourceNode, this);
    }
}
