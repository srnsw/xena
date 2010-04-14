// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * A reverse axis (XPath) expression represents a Node set
 * which may need to be seen in document order (thus reversed)
 */
abstract class ReverseAxisExpr extends AxisExpr 
{

    /**
     * @return a version of this which, when evaluated, returns
     * a Node iterator in document order 
     */
    ConvertibleNodeSetExpr makeDocumentOrderExpr(final ConvertibleNodeSetExpr expr) {
        return new ConvertibleNodeSetExpr() {
                public NodeIterator eval(Node node, 
                                         ExprContext context) 
                    throws XSLException 
                {
                    return reverse(expr.eval(node, context));
                }
            };
    }

    //
    //
    private static NodeIterator reverse(NodeIterator iter)
        throws XSLException 
    {
        Node nodes[] = new Node[10];
        int off = nodes.length;
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            if (off == 0) {
                Node oldNodes[] = nodes;
                nodes = new Node[oldNodes.length * 2];
                System.arraycopy(oldNodes, 0, nodes, oldNodes.length,
                                 oldNodes.length);
                off = oldNodes.length;
            }
            nodes[--off] = node;
        }
        return new ArrayNodeIterator(nodes, off, nodes.length);
    }
}
