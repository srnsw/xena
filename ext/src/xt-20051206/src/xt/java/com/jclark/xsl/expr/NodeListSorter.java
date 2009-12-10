// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.util.MergeSort;
import com.jclark.xsl.util.Comparator;

/**
 * Like the name suggests, provides a mechanism to sort
 * the nodes in a NodeIterator based upon the test of
 * <code>Comparator</code>
 */
public class NodeListSorter 
{
    private NodeListSorter() { }

    static public NodeIterator sort(NodeIterator iter, 
                                    Comparator comparator) 
        throws XSLException 
    {
        Node[] nodes = new Node[10];
        int nNodes = 0;

        // copy the nodes into an array, removing nulls
        for (;;) {
            Node tem = iter.next();
            if (tem == null) {
                break;
            }
            if (nNodes == nodes.length) {
                Node[] old = nodes;
                nodes = new Node[nodes.length * 2];
                System.arraycopy(old, 0, nodes, 0, old.length);
            }
            nodes[nNodes++] = tem;
        }

        // MergeSort will re-order the Nodes in the array
        MergeSort.sort(comparator, nodes, 0, nNodes);
        return new ArrayNodeIterator(nodes, 0, nNodes);
    }

}
