// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * A compiled XPath pattern component which returns a Node set, but is
 * convertible (castable) to a String expression, boolean expression
 * or VariantExpression
 */
abstract class ConvertibleNodeSetExpr 
    extends ConvertibleExpr 
    implements NodeSetExpr
{

    ConvertibleStringExpr makeStringExpr()
    {
        return new ConvertibleStringExpr() {
                public String eval(Node node, 
                                   ExprContext context) 
                    throws XSLException 
                {
                    return Converter.toString(ConvertibleNodeSetExpr.this.eval(node, context));
                }
            };
    }

    /**
     *
     */
    ConvertibleBooleanExpr makeBooleanExpr()
    {
        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, 
                                    ExprContext context) throws XSLException 
                {
                    return Converter.toBoolean(ConvertibleNodeSetExpr.this.eval(node, context));
                }
            };
    }

    /**
     *
     */
    ConvertibleNodeSetExpr makeNodeSetExpr()
    {
        return this;
    }


    /**
     *
     */
    ConvertibleVariantExpr makeVariantExpr()
    {
        return new ConvertibleVariantExpr() 
            {
                public Variant eval(Node node, 
                                    ExprContext context) throws XSLException 
                {
                    return new NodeSetVariant(ConvertibleNodeSetExpr.this.eval(node, 
                                                                               context));
                }
            };
    }


    /**
     * If this is set, then all nodes in the result of eval(x, c)
     * are guaranteed to be in the subtree rooted at x.
     */
    static final int STAYS_IN_SUBTREE = 01;

    /**
     * If this is set, then all nodes in the result of eval(x, c) are
     * guaranteed to be at the same level of the tree. More precisely,
     * define the level of a node to be the number of ancestors it has,
     * and then define an expression to be single-level if and only if
     * there exists an integer n such that for any node x, for any node
     * y in the result of evaluating the expression with respect to x,
     * the difference between the level of x and the level of y is equal
     * to n.  For example, the children axis is single-level but the
     * descendants axis is not.
     */
    static final int SINGLE_LEVEL = 02;

    int getOptimizeFlags()
    {
        return 0;
    }
  
    /**
     * Return an expression for this/expr 
     */
    ConvertibleNodeSetExpr compose(ConvertibleNodeSetExpr expr) 
    {
        int opt1 = this.getOptimizeFlags();
        int opt2 = expr.getOptimizeFlags();
        if ((opt1 & SINGLE_LEVEL) != 0
            && (opt2 & STAYS_IN_SUBTREE) != 0)
            return new SequenceComposeExpr(this, expr);
        return new ComposeExpr(this, expr);
    }

    /**
     *
     */
    Pattern getChildrenNodePattern() 
    {
        return null;
    }
}
