// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class BooleanRelationalExpr extends ConvertibleBooleanExpr 
{
    private final Relation rel;
    private final BooleanExpr expr1;
    private final BooleanExpr expr2;

    BooleanRelationalExpr(Relation rel, BooleanExpr expr1, BooleanExpr expr2) 
    {
        this.rel = rel;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean eval(Node node, ExprContext context) throws XSLException 
    {
        return rel.relate(expr1.eval(node, context), expr2.eval(node, context));
    }
}
