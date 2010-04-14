// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * <xsl:message
 */
class MessageAction implements Action
{
    private Action content;

    MessageAction(Action content)
    {
        this.content = content;
    }

    public void invoke(ProcessContext context, Node sourceNode,
                       Result result) throws XSLException
    {
        StringResult s = new StringResult(result);
        content.invoke(context, sourceNode, s);
        result.message(sourceNode, s.toString());
    }
}
