// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

abstract class ConvertibleStringExpr extends ConvertibleExpr implements StringExpr
{

    ConvertibleStringExpr makeStringExpr()
    {
        return this;
    }

    ConvertibleBooleanExpr makeBooleanExpr()
    {
        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, ExprContext context) throws XSLException {
                    return Converter.toBoolean(ConvertibleStringExpr.this.eval(node, context));
                }
            };
    }

    ConvertibleVariantExpr makeVariantExpr()
    {
        return new ConvertibleVariantExpr() {
                public Variant eval(Node node, ExprContext context) throws XSLException {
                    return new StringVariant(ConvertibleStringExpr.this.eval(node, context));
                }
            };
    }

    ConvertibleNumberExpr makeNumberExpr()
    {
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context) throws XSLException {
                    return Converter.toNumber(ConvertibleStringExpr.this.eval(node, context));
                }
            };
    }

    public String constantValue()
    {
        return null;
    }
}
