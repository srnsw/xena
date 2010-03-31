// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.Node;
import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.expr.StringExpr;

/**
 * represents an "xsl:value-of" with disable-output-escaping='yes'
 */
class RawValueOfAction implements Action
{
    private StringExpr expr;
    private String attribute;

    /**
     * construct around the given StringExpr
     */
    RawValueOfAction(StringExpr expr)
    {
        this.expr = expr;

        // Huh??
        this.attribute = attribute;
    }

    /**
     * evaluate with the given context Node and
     * ProcessContext, sending the results to the given result
     */
    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) 
        throws XSLException
    {
        result.rawCharacters(expr.eval(sourceNode, context));
    }

}
