// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class CloneableNodeIteratorImpl implements CloneableNodeIterator
{

    private final NodeList list;
    private int i;

    public Object clone() {
        return new CloneableNodeIteratorImpl(list, i);
    }
   
    public CloneableNodeIteratorImpl(NodeIterator iter) {
        list = new NodeList(iter);
        i = 0;
    }

    private CloneableNodeIteratorImpl(NodeList list, int i) {
        this.list = list;
        this.i = i;
    }

    public Node next() throws XSLException {
        Node tem = list.nodeAt(i);
        if (tem != null)
            i++;
        return tem;
    }

    public void bind() throws XSLException {
        for (int i = 0; list.nodeAt(i) != null; i++)
            ;
    }

    //////////////////////////////
    static class NodeList {
        final NodeIterator iter;
        Node[] nodes = null;
        int len = 0;
        NodeList(NodeIterator iter) {
            this.iter = iter;
        }
        Node nodeAt(int i) throws XSLException {
            if (i >= len) {
                if (nodes == null)
                    nodes = new Node[i + 4];
                else if (i >= nodes.length) {
                    Node[] oldNodes = nodes;
                    nodes = new Node[oldNodes.length*2];
                    System.arraycopy(oldNodes, 0, nodes, 0, oldNodes.length);
                }
                // Have i < nodes.length
                for (; len <= i; len++) {
                    if ((nodes[len] = iter.next()) == null)
                        return null;
                }
                // Have i < len
            }
            return nodes[i];
        }
    }


}
