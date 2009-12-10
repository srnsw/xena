// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class SingleNodeIterator implements NodeIterator {
    private Node node;
    public SingleNodeIterator(Node node) {
        this.node = node;
    }
    public Node next() {
        Node tem = node;
        node = null;
        return tem;
    }
}
