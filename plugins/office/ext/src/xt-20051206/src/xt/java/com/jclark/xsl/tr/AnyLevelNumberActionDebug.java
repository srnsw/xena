// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.conv.NumberListFormat;
import com.jclark.xsl.expr.Pattern;
import com.jclark.xsl.expr.DescendantsOrSelfNodeIterator;
import java.util.Hashtable;

class AnyLevelNumberActionDebug extends AnyLevelNumberAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    AnyLevelNumberActionDebug(
                              ActionDebugTarget target, 
                              Node sheetNode,
                              String templateIDHook,
                              Pattern count, Pattern from,
                              NumberListFormatTemplate formatTemplate)
    {
        super(count, from, formatTemplate);

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
