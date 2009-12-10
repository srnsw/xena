// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class NullNodeIterator implements NodeIterator {
    public Node next() { return null; }
}
