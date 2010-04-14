// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class PrecedingSiblingAxisExpr extends ReverseAxisExpr 
{
    public NodeIterator eval(Node node, ExprContext context) throws XSLException 
    {
        return precedingSiblings(node);
    }

    static NodeIterator precedingSiblings(Node node) throws XSLException 
    {
        if (node.getType() == Node.ATTRIBUTE) {
            return null;
	}
        Node tem = node.getParent();
        if (tem == null) {
            return new SingleNodeIterator(null);
	}
        NodeIterator iter = tem.getChildren();
        tem = iter.next();
        if (tem.equals(node)) {
            return new SingleNodeIterator(null);
	}
        Node[] nodes = new Node[1];
        int off = nodes.length;
        nodes[--off] = tem;
        for (tem = iter.next(); !tem.equals(node); tem = iter.next()) {
            if (off == 0) {
                Node oldNodes[] = nodes;
                nodes = new Node[oldNodes.length * 2];
                System.arraycopy(oldNodes, 0, nodes, oldNodes.length,
				 oldNodes.length);
                off = oldNodes.length;
            }
            nodes[--off] = tem;
        }
        return new ArrayNodeIterator(nodes, off, nodes.length);
    }

    int getOptimizeFlags() 
    {
        return SINGLE_LEVEL;
    }
}
