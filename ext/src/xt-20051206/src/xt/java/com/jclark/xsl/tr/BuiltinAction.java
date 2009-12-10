// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class BuiltinAction implements Action
{
    final Name modeName;

    BuiltinAction(Name modeName)
    {
        this.modeName = modeName;
    }

    BuiltinAction()
    {
        this.modeName = null;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        switch (sourceNode.getType()) {
        case Node.ELEMENT:
        case Node.ROOT:
            // I guess we recurse
            context.process(sourceNode.getChildren(), 
                            modeName, null, null, result);
            break;
        case Node.TEXT:
        case Node.ATTRIBUTE:
            result.characters(sourceNode.getData());
            break;
        }
    }
}
