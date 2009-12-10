// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * an XPath top level match pattern?
 */
public interface TopLevelPattern extends Pattern 
{
    /**
     * may represent an "or" grouping, so
     *  we allow them to be broken
     * out and dealt with separately
     */
    PathPattern[] getAlternatives();
}
