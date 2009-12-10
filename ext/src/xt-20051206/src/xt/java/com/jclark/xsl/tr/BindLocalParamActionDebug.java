// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;

/**
 * binds a local parameter to a name
 * <xsl:param />
 */
class BindLocalParamActionDebug extends BindLocalParamAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    BindLocalParamActionDebug(
                               ActionDebugTarget target, 
                               Node sheetNode,
                               String templateIDHook,
                              Name name, VariantExpr expr)
    {
        super(name, expr);

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
