// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NamespaceTest implements Pattern 
{
    private final String ns;

    NamespaceTest(String ns) {
        this.ns = ns;
    }

    public boolean matches(Node node, ExprContext context) {
        return ns.equals(node.getName().getNamespace());
    }
}
