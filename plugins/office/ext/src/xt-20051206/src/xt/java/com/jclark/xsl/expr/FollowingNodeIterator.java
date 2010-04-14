// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class FollowingNodeIterator implements NodeIterator 
{
    private NodeIterator iter;
    private Node parent;

    public FollowingNodeIterator(Node node) 
    {
        if (node.getType() == Node.ATTRIBUTE) {
            node = node.getParent();
	}
        parent = node.getParent();
        // Don't blow up on the root node
        if (parent == null) {
            parent = node;
	}
        iter = node.getFollowingSiblings();
    }

    public Node next() throws XSLException {
        for (;;) {
            Node node = iter.next();
            if (node != null) {
                parent = node;
                iter = node.getChildren();
                return node;
            }
            else {
                iter = parent.getFollowingSiblings();
                node = parent.getParent();
                if (node == null) {
                    break;
		}
                parent = node;
            }
        }
        return null;
    }
}
