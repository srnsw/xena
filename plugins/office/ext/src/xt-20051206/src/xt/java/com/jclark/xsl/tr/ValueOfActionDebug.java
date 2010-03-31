// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * xsl:value-of
 */
class ValueOfActionDebug extends ValueOfAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    public ValueOfActionDebug(
                              ActionDebugTarget target, 
                              Node sheetNode,
                              String templateIDHook,
                              StringExpr expr)
    {
        super(expr);

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
        super.invoke(context, sourceNode, _target);

        _target.endAction(_sheetNode, sourceNode, this);
    }


}
