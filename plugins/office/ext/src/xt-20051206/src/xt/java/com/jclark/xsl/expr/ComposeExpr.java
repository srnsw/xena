// $Id$

package com.jclark.xsl.expr;
import com.jclark.xsl.om.*;

/**
 * an expression which composes two sub-expressions
 *  (for each node in expr1, evaluate expr2)
 */
class ComposeExpr extends ConvertibleNodeSetExpr
{
    private final ConvertibleNodeSetExpr expr1;
    private final ConvertibleNodeSetExpr expr2;

    /**
     * construct with two sub-expressions
     */
    ComposeExpr(ConvertibleNodeSetExpr expr1, 
                ConvertibleNodeSetExpr expr2) 
    {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    /**
     * evaluate with a context node and an expression context
     */
    public NodeIterator eval(Node node, 
                             ExprContext context) 
        throws XSLException 
    {

        NodeIterator iter = expr1.eval(node, context);
        NodeIterator[] iters = new NodeIterator[10];
        int length = 0;
        for (;;) {
            // for each node in the first expression
            // we build a NodeIterator for the second expression
            Node tem = iter.next();
            if (tem == null) {
                // we've exhausted our supply of nodes in the 
                //  first expression
                break;
            }
            if (length == iters.length) {
                // we need a bigger array
                NodeIterator[] oldIters = iters;
                iters = new NodeIterator[oldIters.length * 2];
                System.arraycopy(oldIters, 0, iters, 0, oldIters.length);
            }
            iters[length++] = expr2.eval(tem, context);
        }

        // so, how many iterators did we build?
        switch (length) {
        case 0:
            return new NullNodeIterator();
        case 1:
            return iters[0];
        case 2:
            return new UnionNodeIterator(iters[0], iters[1]);
        }
        return new MergeNodeIterator(iters, length);
    }

    /**
     *
     */
    int getOptimizeFlags() 
    {
        return expr1.getOptimizeFlags() & expr2.getOptimizeFlags();
    }
}
