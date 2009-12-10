// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * a no-op
 */
class EmptyActionDebug extends EmptyAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    EmptyActionDebug(ActionDebugTarget target,
                     Node sheetNode,
                     String templateIDHook)
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
        //        throws XSLException
    {
        _target.startAction(_sheetNode, sourceNode, this);

        super.invoke(context, sourceNode, result);

        _target.endAction(_sheetNode, sourceNode, this);
    }

}
