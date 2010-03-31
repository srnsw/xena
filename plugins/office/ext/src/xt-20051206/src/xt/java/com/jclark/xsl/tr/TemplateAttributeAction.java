// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

/**
 *
 */
class TemplateAttributeAction implements Action
{
    private Name name;
    private StringExpr value;

    TemplateAttributeAction(Name name, StringExpr value)
    {
        this.name = name;
        this.value = value;
    }

    public void invoke(ProcessContext context, Node sourceNode, Result result)
        throws XSLException
    {
        result.attribute(context.unaliasName(name),
                         value.eval(sourceNode, context));
    }
}
