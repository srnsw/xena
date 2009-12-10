// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class ElementOrAttributeTest implements Pattern 
{
    private final Name name;

    ElementOrAttributeTest(Name name) 
    {
        this.name = name;
    }

    public boolean matches(Node node, ExprContext context) 
    {
        return (name.equals(node.getName())
                && node.getType() != Node.PROCESSING_INSTRUCTION);
    }
}
