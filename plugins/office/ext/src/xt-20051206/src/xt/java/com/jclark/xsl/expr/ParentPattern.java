// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * represents the concatenation of step patterns, right to left
 * in a LocationPathPattern  
 */
class ParentPattern extends PathPatternBase 
{
    private PathPatternBase childPattern;
    private Pattern parentPattern;

    /**
     * construct with a new stepPattern: childPattern and a previous parentPattern
     */
    ParentPattern(PathPatternBase childPattern, Pattern parentPattern) 
    {
	// the right hand (child or attribute axis) StepPattern
        this.childPattern = childPattern;

	// whatever came before
        this.parentPattern = parentPattern;
    }

    /**
     *  if the rightmost step matches, and our parentPattern's matches() returns true for
     *   this node's parent then we have a winner!
     */
    public boolean matches(Node node, ExprContext context) throws XSLException 
    {
        if (!childPattern.matches(node, context)) {
            return false;
	}
        node = node.getParent();
        if (node == null) {
	    // we ran out of ancestors before we ran out of StepPatterns
            return false;
	}
        return parentPattern.matches(node, context);
    }

    public int getDefaultPriority() 
    {
        return 1;
    }

    /**
     * gets the rightmost (final) step's matchNodeType
     */
    Name getMatchName() 
    {
        return childPattern.getMatchName();
    }

    /**
     * gets the rightmost (final) step's matchNodeType
     */
    byte getMatchNodeType() 
    {
        return childPattern.getMatchNodeType();
    }
}
