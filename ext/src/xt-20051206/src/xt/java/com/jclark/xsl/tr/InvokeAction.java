// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;
import java.util.Hashtable;

/**
 * call template
 */
class InvokeAction extends ParamAction
{
    private Name name;
    private Hashtable namedTemplateTable;

    InvokeAction(Name name, Hashtable namedTemplateTable) throws XSLException
    {
        this.name = name;
        this.namedTemplateTable = namedTemplateTable;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result)
        throws XSLException
    {
        Action action = (Action)namedTemplateTable.get(name);
        if (action == null) {
            throw new XSLException(name.toString() +
                                   ": no such named template");
        }
        context.invokeWithParams(action,
                                 getParamNames(),
                                 getParamValues(sourceNode, context),
                                 sourceNode,
                                 result);
    }
}
