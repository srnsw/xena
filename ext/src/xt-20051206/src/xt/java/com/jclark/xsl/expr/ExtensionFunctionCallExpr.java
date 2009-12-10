// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class ExtensionFunctionCallExpr extends ConvertibleVariantExpr 
{
    private final Name name;
    private final VariantExpr[] args;

    ExtensionFunctionCallExpr(Name name, VariantExpr[] args) 
    {
        this.name = name;
        this.args = args;
    }

    public Variant eval(Node node, ExprContext context) throws XSLException 
    {
        Object[] argValues = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            argValues[i] = args[i].eval(node, context).convertToObject();
        }
        Object obj
            = context.getExtensionContext(name.getNamespace()).call(name.getLocalPart(),
                                                                    node,
                                                                    argValues);
        return VariantBase.create(obj);
    }
}
