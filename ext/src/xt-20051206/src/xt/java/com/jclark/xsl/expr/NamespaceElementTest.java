// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NamespaceElementTest extends PathPatternBase
{
    private final String ns;

    NamespaceElementTest(String ns)
    {
        this.ns = ns;
    }

    public boolean matches(Node node, ExprContext context)
    {
        return node.getType() == Node.ELEMENT && ns.equals(node.getName().getNamespace());
    }

    byte getMatchNodeType() 
    { return Node.ELEMENT; }

    public int getDefaultPriority() 
    { return -1; }
}
