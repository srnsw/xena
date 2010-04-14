// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 * <xt:document -- XT extension element writes output to a url
 */
class DocumentAction implements Action
{
    private StringExpr hrefExpr;
    private OutputMethod outputMethod;
    private Action content;

    /**
     * construct with a uri to write to, the type of output desired, and
     *  the contect to emit
     */
    DocumentAction(StringExpr hrefExpr,
                   OutputMethod outputMethod,
                   Action content)
    {
        this.hrefExpr = hrefExpr;
        this.outputMethod = outputMethod;
        this.content = content;
    }

    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        result = result.createResult(hrefExpr.eval(sourceNode, context));
        if (result != null) {
            result.start(outputMethod);
            content.invoke(context, sourceNode, result);
            result.end();
        } else {
            // FIXME: debugging code
            System.err.println("DocumentAction::cannot create Result for writing");
        }
    }
}
