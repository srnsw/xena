// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class ApplyImportsActionDebug extends ApplyImportsAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    public ApplyImportsActionDebug(
                                   ActionDebugTarget target, 
                                   Node sheetNode,
                                   String templateIDHook
                                   )
    {
        super();

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
