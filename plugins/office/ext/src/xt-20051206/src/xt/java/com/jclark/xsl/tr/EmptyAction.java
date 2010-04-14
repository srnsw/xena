// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * a no-op
 */
class EmptyAction implements Action
{
    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) { }
}
