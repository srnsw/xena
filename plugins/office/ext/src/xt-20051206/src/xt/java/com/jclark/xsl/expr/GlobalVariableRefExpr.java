// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class GlobalVariableRefExpr extends ConvertibleVariantExpr {
    private final Name name;
    private final Node node;

    GlobalVariableRefExpr(Name name, Node node) {
        this.name = name;
        this.node = node;
    }

    public Variant eval(Node sourceNode, ExprContext context) throws XSLException {
        Variant value = context.getGlobalVariableValue(name);
        if (value != null) {
            return value;
        }
        throw new XSLException("variable \"" + name + "\" not defined",
                               node);
    }

    public Name getName() { return name; }
}
