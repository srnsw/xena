// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * does some sort of sort/merge on NodeIterators, I think
 */
class MergeNodeIterator implements NodeIterator 
{
    private NodeIterator[] iters;
    private Node[] nodes;
    private int length;

    /** 
     * construct with an array of iterators
     * 
     * @param length the number of slots in the array
     *  which really have NodeIterators for us
     */  
    MergeNodeIterator(NodeIterator[] iters, int length) 
        throws XSLException 
    {
        this.length = length;
        this.iters = iters;
        nodes = new Node[length];
        int j = 0;
        for (int i = 0; i < length; i++) {
            // we squeeze out NodeIterators with no nodes
            // and put the first node from each iterator
            // in our "nodes" array
            if (i != j) {
                iters[j] = iters[i];
            }
            Node tem = iters[j].next();
            if (tem != null) {
                nodes[j++] = tem;
            }
        }
        this.length = j; // reset the length to reflect squeezing
        buildHeap();
    }

    /**
     * Make the heap rooted at i a heap, assuming its
     * children are heaps. 
     */
    private final void heapify(int i) 
    {
        // i starts out around (length / 2) - 1
        for (;;) {
            int left = (i << 1) | 1; // (i*2) + 1 ??
            int right = left + 1;    // (i*2) + 2 ??

            if (right < length) {

                if (compare(left, right) <= 0) {
                    // left <= right
 
                   if (compare(left, i) > 0) {
                        break;
                    }
                    exchange(left, i);
                    i = left;
                }
                else {
                    // right >= left
                    if (compare(right, i) > 0) {
                        break;
                    }
                    exchange(right, i);
                    i = right;
                }
            }
            else if (left < length) {
                if (compare(left, i) > 0) {
                    break;
                }
                exchange(left, i);
                i = left;
            }
            else {
                break;
            }
        }
    }

    /**
     * swaps the items with the given indices
     */
    private final void exchange(int i, int j) 
    {
        {
            Node tem = nodes[i];
            nodes[i] = nodes[j];
            nodes[j] = tem;
        }
        {
            NodeIterator tem = iters[i];
            iters[i] = iters[j];
            iters[j] = tem;
        }
    }

    private final int compare(int i, int j) 
    {
        return nodes[i].compareTo(nodes[j]);
    }

    private void buildHeap() 
    {
        for (int i = length/2 - 1; i >= 0; --i) {
            heapify(i);
        }
    }

    /**
     * finds and returns the next node (in document(s) order?)
     */
    public Node next() throws XSLException 
    {
        if (length == 0) {
            return null;
        }
        Node max = nodes[0];
        do {
            Node tem = iters[0].next();
            if (tem == null) {
                if (--length == 0)
                    break;
                nodes[0] = nodes[length];
                iters[0] = iters[length];
            }
            else {
                nodes[0] = tem;
            }
            heapify(0);
        } while (max.equals(nodes[0]));
        return max;
    }
}
