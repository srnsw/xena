// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import java.util.StringTokenizer;
import java.util.Enumeration;

class ValueIdIterator implements NodeIterator {
    private Node node;
    private NodeIterator iter1;
    private NodeIterator iter2;

    ValueIdIterator(Node node, NodeIterator iter1) {
        this.node = node;
        this.iter1 = iter1;
        this.iter2 = new NullNodeIterator();
    }

    static class Iterator implements NodeIterator {
        private Node node;
        private Enumeration ids;
        Iterator(Node node, String str) {
            this.node = node;
            ids = new StringTokenizer(str);
        }
        public Node next() {
            while (ids.hasMoreElements()) {
                Node tem = node.getElementWithId((String)ids.nextElement());
                if (tem != null)
                    return tem;
            }
            return null;
        }
    }

    public Node next() throws XSLException {
        for (;;) {
            Node tem = iter2.next();
            if (tem != null)
                return tem;
            tem = iter1.next();
            if (tem == null)
                break;
            iter2 = new Iterator(node, Converter.toString(tem));
        }
        return null;
    }
}
