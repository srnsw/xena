// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;
import java.util.Hashtable;

/**
 * call template
 */
class InvokeActionDebug extends InvokeAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;


    InvokeActionDebug(
                      ActionDebugTarget target, 
                      Node sheetNode,
                      String templateIDHook,
                      Name name, 
                      Hashtable namedTemplateTable) throws XSLException
    {
        super(name, namedTemplateTable);

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
