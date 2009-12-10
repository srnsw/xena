// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class WithCurrentExpr extends ConvertibleExpr 
{
    private final ConvertibleExpr expr;

    class Context extends DelegateExprContext 
    {
        Node node;

        Context(Node node, ExprContext context) {
            super(context);
            this.node = node;
        }

        public Node getCurrent(Node contextNode) {
            return node;
        }
    }

    WithCurrentExpr(ConvertibleExpr expr) 
    {
        this.expr = expr;
    }

    ConvertibleStringExpr makeStringExpr() 
    {
        final StringExpr e = expr.makeStringExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return e.eval(node, new Context(node, context));
                }
                public String constantValue() {
                    return e.constantValue();
                }
            };
    }

    ConvertibleVariantExpr makeVariantExpr() {
        final VariantExpr e = expr.makeVariantExpr();
        return new ConvertibleVariantExpr() {
                public Variant eval(Node node, ExprContext context) throws XSLException {
                    return e.eval(node, new Context(node, context));
                }
            };
    }

    ConvertibleNodeSetExpr makeNodeSetExpr() throws ParseException {
        final NodeSetExpr e = expr.makeNodeSetExpr();
        return new ConvertibleNodeSetExpr() {
                public NodeIterator eval(Node node, ExprContext context) throws XSLException {
                    return e.eval(node, new Context(node, context));
                }
            };
    }

    ConvertibleNumberExpr makeNumberExpr() {
        final NumberExpr e = expr.makeNumberExpr();
        return new ConvertibleNumberExpr() {
                public double eval(Node node, ExprContext context) throws XSLException {
                    return e.eval(node, new Context(node, context));
                }
            };
    }

    ConvertibleBooleanExpr makeBooleanExpr() {
        final BooleanExpr e = expr.makeBooleanExpr();
        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, ExprContext context) throws XSLException {
                    return e.eval(node, new Context(node, context));
                }
            };
    }
}
