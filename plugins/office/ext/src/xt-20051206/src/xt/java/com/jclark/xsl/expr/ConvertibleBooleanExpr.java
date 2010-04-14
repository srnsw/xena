// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * An expression which is a boolean, but depending
 * on its use, may be converted to a Number, String or Object
 */
abstract class ConvertibleBooleanExpr extends ConvertibleExpr 
    implements BooleanExpr
{

    ConvertibleStringExpr makeStringExpr() 
    {
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context)
		    throws XSLException
		{
                    return Converter.toString(ConvertibleBooleanExpr.this.eval(node, context));
                }
            };
    }

    ConvertibleNumberExpr makeNumberExpr() 
    {
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context)
		    throws XSLException 
		{
                    return Converter.toNumber(ConvertibleBooleanExpr.this.eval(node, context));
                }
            };
    }

    ConvertibleBooleanExpr makeBooleanExpr()
    {
        return this;
    }

    ConvertibleVariantExpr makeVariantExpr()
    {
        return new ConvertibleVariantExpr() {
                public Variant eval(Node node, ExprContext context)
		    throws XSLException 
		{
                    return new BooleanVariant(ConvertibleBooleanExpr.this.eval(node, context));
                }
            };
    }
}
