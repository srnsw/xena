// $Id$

package com.jclark.xsl.expr;
import com.jclark.xsl.om.*;

/**
 * represents an "OR" (union) of match patterns
 */
class AlternativesPattern implements TopLevelPattern
{
    private TopLevelPattern pattern1;
    private PathPattern pattern2;
  
    /**
     * construct with a head pattern1 and tail pattern2
     */
    AlternativesPattern(TopLevelPattern pattern1, 
                        PathPattern pattern2)
    {
        this.pattern1 = pattern1;
        this.pattern2 = pattern2;
    }

    /**
     * evaluate to a boolean
     */
    public boolean matches(Node node, 
                           ExprContext context) 
        throws XSLException
    {
        return pattern1.matches(node, context) || 
            pattern2.matches(node, context);
    }

    /**
     * @return an array of all the alternative PathPatterns
     */
    public PathPattern[] getAlternatives()
    {
	// we decompose a backwards sort of lisp-like list
        PathPattern[] tem = pattern1.getAlternatives();
        PathPattern[] result = new PathPattern[tem.length + 1];
        System.arraycopy(tem, 0, result, 0, tem.length);
        result[result.length - 1] = pattern2;
        return result;
    }
}


