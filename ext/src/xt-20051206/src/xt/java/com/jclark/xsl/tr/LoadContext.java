// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.Name;

/**
 * maintains a list of a few options for how a XMLProcessor constructs
 * an object model
 */
public interface LoadContext
{
    /**
     * white space handling
     */
    boolean getStripSource(Name elementTypeName);

    /**
     * include comment nodes?
     */
    boolean getIncludeComments();

    /**
     * include processing instructions?
     */
    boolean getIncludeProcessingInstructions();

    /**
     * are we instrumenting this transformer?
     */
    ActionDebugTarget getDebugger();
}
