// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * a compiled XPath expression which has an
 * XSLT match priority
 */
public interface PathPattern extends Pattern 
{
    /**
     * priority when used in XSLT template match pattern
     */
    int getDefaultPriority();
}
