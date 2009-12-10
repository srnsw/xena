// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * a compiled XPath expression (component) that represents
 * the addition of two sub-expressions
 */
class AddExpr extends ConvertibleNumberExpr
{
    private final NumberExpr expr1;
    private final NumberExpr expr2;

    /**
     * construct with two NumberExpr(essions)
     */
    AddExpr(NumberExpr expr1, NumberExpr expr2)
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    /**
     * evaluate to result of a double
     */
    public double eval(Node node, ExprContext context) throws XSLException
    {
        return expr1.eval(node, context) + expr2.eval(node, context);
    }
}
