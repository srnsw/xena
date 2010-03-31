// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.Node;
import com.jclark.xsl.om.XSLException;


/**
 * Represents the concatenation of two String Expressions
 */
class AppendExpr extends ConvertibleStringExpr
{
    private StringExpr expr1;
    private StringExpr expr2;

    /**
     * construct with two XPath expressions which evaluate
     * to Strings
     */
    AppendExpr(StringExpr expr1, StringExpr expr2)
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    /**
     * evaluate each of the two sub-expressions with the given
     * context Node and given context, return the concatenation
     * of the results of each evaluation
     */ 
    public String eval(Node node, ExprContext context) throws XSLException
    {
        return expr1.eval(node, context) + expr2.eval(node, context);
    }
}
