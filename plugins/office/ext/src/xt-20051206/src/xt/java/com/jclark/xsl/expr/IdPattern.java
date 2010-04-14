// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * represents an XSLT/XPath pattern of the form id('foo')
 */
class IdPattern extends PathPatternBase 
{
    private String id;

    // FIXME: allow it to be a whitespace-separated list
    IdPattern(String id) 
    {
        this.id = id;
    }

    /**
     * @return true if the given node carries the id under consideration
     */
    public boolean matches(Node node, ExprContext context) 
    {
        return node.isId(id);
    }

    /**
     * @return om.Node.ELEMENT
     */
    byte getMatchNodeType() 
    {
        return Node.ELEMENT;
    }

    /**
     *
     */
    public int getDefaultPriority() 
    { return 1; }
}


