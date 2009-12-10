// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**  The algorithm is:
<pre>
(define (preceding x)
  (define (reverse-subtree x)
    (append (map reverse-subtree (reverse (children x)))
	    (list x)))
  (map (lambda (y)
	 (map reverse-subtree (preceding-sibling y)))
       (ancestor-or-self x)))
</pre>
*/
    
class PrecedingAxisExpr extends ReverseAxisExpr 
{
    static class AppendNodeIterator implements NodeIterator 
    {
	private NodeIterator iter;
	private Node node;
	AppendNodeIterator(NodeIterator iter, Node node) {
	    this.iter = iter;
	    this.node = node;
	}
	public Node next() throws XSLException {
	    Node tem = iter.next();
	    if (tem == null) {
		tem = node;
		node = null;
	    }
	    return tem;
	}
    }
    
    static class ReverseSubtreeExpr implements NodeSetExpr 
    {
	public NodeIterator eval(Node node, ExprContext context) throws XSLException 
        {
	    SafeNodeIterator children = node.getChildren();
	    Node child1 = children.next();
	    if (child1 == null) {
		return new SingleNodeIterator(node);
            }
	    Node child2 = children.next();
	    if (child2 == null) {
		return new AppendNodeIterator(eval(child1, context), node);
            }
	    Node[] nodes = new Node[2];
	    nodes[0] = child2;
	    nodes[1] = child1;
	    int off = 0;
	    for (Node tem = children.next(); tem != null; tem = children.next()) {
		if (off == 0) {
		    Node oldNodes[] = nodes;
		    nodes = new Node[oldNodes.length * 2];
		    System.arraycopy(oldNodes, 0, nodes, oldNodes.length, 
				     oldNodes.length);
		    off = oldNodes.length;
		}
		nodes[--off] = tem;
	    }

	    return new AppendNodeIterator(new SequenceComposeNodeIterator(new ArrayNodeIterator(nodes, 
                                                                                                off, 
                                                                                                nodes.length),
									  new ReverseSubtreeExpr(),
									  context),
					  node);
	}
    }
    
    static class LeftExpr implements NodeSetExpr 
    {
	public NodeIterator eval(Node node, ExprContext context) throws XSLException 
	{
	    return new SequenceComposeNodeIterator(PrecedingSiblingAxisExpr.precedingSiblings(node),
						   new ReverseSubtreeExpr(),
						   context);
	}
    }
    
    public NodeIterator eval(Node node, ExprContext context) throws XSLException 
    {
	return new SequenceComposeNodeIterator(new AncestorsOrSelfNodeIterator(node),
					       new LeftExpr(),
					       context);
    }

}


