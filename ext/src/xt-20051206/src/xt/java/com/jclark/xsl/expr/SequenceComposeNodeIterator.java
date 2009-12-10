// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class SequenceComposeNodeIterator implements NodeIterator 
{
    private final NodeIterator iter1;
    private NodeIterator iter2;
    private final NodeSetExpr expr;
    private final ExprContext context;

    SequenceComposeNodeIterator(NodeIterator iter, NodeSetExpr expr,
				ExprContext context) 
    {
        this.iter1 = iter;
        this.expr = expr;
        this.context = context;
        this.iter2 = new NullNodeIterator();
    }

    public Node next() throws XSLException 
    {
        for (;;) {
            Node node = iter2.next();
            if (node != null)
                return node;
            node = iter1.next();
            if (node == null)
                break;
            iter2 = expr.eval(node, context);
        }
        return null;
    }
}
