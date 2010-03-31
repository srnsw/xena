// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * the XPath function id(x)
 */
class IdFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException 
    {
        if (e instanceof NodeSetExpr) {
            final NodeSetExpr nse = (NodeSetExpr)e;
            return new ConvertibleNodeSetExpr() {
                    public NodeIterator eval(Node node, ExprContext context) throws XSLException {
                        return id(node, nse.eval(node, context));
                    }
                };
        }
        else if (e instanceof VariantExpr) {
            final VariantExpr ve = (VariantExpr)e;
            return new ConvertibleNodeSetExpr() {
                    public NodeIterator eval(Node node, ExprContext context) throws XSLException {
                        Variant v = ve.eval(node, context);
                        if (v.isNodeSet()) {
                            return id(node, v.convertToNodeSet());
                        } else {
                            return id(node, v.convertToString());
                        }
                    }
                };
        }
        else {
            final StringExpr se = e.makeStringExpr();
            return new ConvertibleNodeSetExpr() {
                    public NodeIterator eval(Node node, ExprContext context) 
                        throws XSLException {
                        return id(node, se.eval(node, context));
                    }
                };
        }
    }
  
    static private final NodeIterator id(Node node, NodeIterator iter) throws XSLException 
    {
        return new UniqueNodeIterator(NodeListSorter.sort(new ValueIdIterator(node, iter),
                                                          new DocumentOrderComparator()));
    }

    static private final NodeIterator id(Node node, String str) throws XSLException 
    {
        return new UniqueNodeIterator(NodeListSorter.sort(new ValueIdIterator.Iterator(node, str),
                                                          new DocumentOrderComparator()));
    }

}
