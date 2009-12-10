// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * Simply tests if a node is of a given type e.g. "text()"
 */
class NodeTypeTest extends PathPatternBase 
{
    private final byte type;

    /**
     * construct with one of the constants from om.Node
     */
    NodeTypeTest(byte type) 
    {
        this.type = type;
    }

    /**
     * return true if this represents the "node()" test, else
     * return true if the node's type matches the test
     */
    public boolean matches(Node node, ExprContext context) 
    {
        return type == Node.ALLTYPES ? true : node.getType() == type;
    }

    /**
     * @return one of the constants on om.Node ... the type of node we match
     */
    byte getMatchNodeType() 
    {
        return type;
    }

    public int getDefaultPriority() { return -2; }
}
