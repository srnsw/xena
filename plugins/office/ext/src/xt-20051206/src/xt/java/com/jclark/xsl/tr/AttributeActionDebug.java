// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

class AttributeActionDebug extends AttributeAction
{


    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    AttributeActionDebug(
                               ActionDebugTarget target, 
                               Node sheetNode,
                               String templateIDHook,
                         StringExpr nameExpr, StringExpr namespaceExpr,
                         NamespacePrefixMap nsMap, Action content)
    {

        super(nameExpr, namespaceExpr, nsMap, content);

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
