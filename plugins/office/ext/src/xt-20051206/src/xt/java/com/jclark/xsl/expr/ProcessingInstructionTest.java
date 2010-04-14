// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class ProcessingInstructionTest extends PathPatternBase 
{
    private final Name name;

    ProcessingInstructionTest(Name name) 
    {
        this.name = name;
    }

    public boolean matches(Node node, ExprContext context) 
    {
        return (name.equals(node.getName())
                && node.getType() == Node.PROCESSING_INSTRUCTION);
    }

    Name getMatchName() 
    { return name; }

    byte getMatchNodeType() 
    { return Node.PROCESSING_INSTRUCTION; }

    public int getDefaultPriority() 
    { return 0; }
}
