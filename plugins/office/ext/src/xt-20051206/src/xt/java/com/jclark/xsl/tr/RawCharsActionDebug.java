// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class RawCharsActionDebug extends RawCharsAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;


    RawCharsActionDebug(ActionDebugTarget target, 
                        Node sheetNode,
                        String templateIDHook,
                        String chars)
    {
        super(chars);

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
