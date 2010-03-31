// $Id$

package com.jclark.xsl.expr;
import com.jclark.xsl.om.*;

/**
 * A compiled XPath component that tests
 * a node to see if it is an attribute
 * with a (possibly?) specified name
 */
class AttributeTest extends PathPatternBase
{
    private final Name name;

    /**
     * construct with the given name
     */
    AttributeTest(Name name)
    {
        this.name = name;
    }

    /**
     * @return true if the node is an ATTRIBUTE and has the
     *   specified Name
     */
    public boolean matches(Node node, ExprContext context)
    {
        return (node.getType() == Node.ATTRIBUTE &&
                name.equals(node.getName()));
    }

    /**
     * @return the Name we test for
     */
    Name getMatchName() 
    { return name; }

    /**
     * @return om.Node.ATTRIBUTE
     */
    byte getMatchNodeType() 
    { return Node.ATTRIBUTE; }

    /**
     * @return 0
     */
    public int getDefaultPriority() 
    { return 0; }
}
