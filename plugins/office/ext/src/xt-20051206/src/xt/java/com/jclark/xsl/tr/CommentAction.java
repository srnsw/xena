// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * <xsl:comment
 */
class CommentAction implements Action
{
    private Action content;

    CommentAction(Action content)
    {
        this.content = content;
    }

    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        StringResult s = new StringResult(result);
        content.invoke(context, sourceNode, s);
        result.comment(s.toString());
    }
}
