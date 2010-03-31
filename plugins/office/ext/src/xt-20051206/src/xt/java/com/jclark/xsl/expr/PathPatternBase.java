// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * a (component of) a compiled XPath pattern expression
 */
abstract class PathPatternBase 
    implements PathPattern, TopLevelPattern 
{
    /**
     * by default, only return a list of length one (itself)
     */
    public PathPattern[] getAlternatives() 
    {
        return new PathPattern[]{this};
    }

    /**
     * by default, returns null
     * Element, Attribute and PI nodetype tests will override this
     */
    Name getMatchName() 
    { return null; }

    /**
     * @returns one of the constants from om.Node
     */
    abstract byte getMatchNodeType();
}
