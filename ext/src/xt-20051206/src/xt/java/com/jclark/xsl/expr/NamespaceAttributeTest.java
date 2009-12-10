// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NamespaceAttributeTest extends PathPatternBase
{
    private final String ns;

    NamespaceAttributeTest(String ns)
    {
        this.ns = ns;
    }

    public boolean matches(Node node, ExprContext context)
    {
        return node.getType() == Node.ATTRIBUTE && ns.equals(node.getName().getNamespace());
    }

    byte getMatchNodeType() { return Node.ATTRIBUTE; }

    public int getDefaultPriority() { return -1; }
}
