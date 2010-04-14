// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class TerminateMessageAction implements Action
{
    private Action content;

    TerminateMessageAction(Action content)
    {
        this.content = content;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) throws XSLException
    {
        StringResult s = new StringResult(result);
        content.invoke(context, sourceNode, s);
        throw new TerminateXSLException(s.toString(), sourceNode);
    }
}
