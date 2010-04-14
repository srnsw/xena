// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * <xt:document -- XT extension element writes output to a url
 */
class DocumentActionDebug extends DocumentAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    /**
     * construct with a uri to write to, the type of output desired, and
     *  the contect to emit
     */
    DocumentActionDebug(
                        ActionDebugTarget target, 
                        Node sheetNode,
                        String templateIDHook,
                        StringExpr hrefExpr,
                        OutputMethod outputMethod,
                        Action content)
    {
        super(hrefExpr, outputMethod, content);

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
