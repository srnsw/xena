// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class UseAttributeSetAction implements Action
{
    private final Name name;

    UseAttributeSetAction(Name name)
    {
        this.name = name;
    }

    public void invoke(ProcessContext context, Node sourceNode,
                       Result result)
        throws XSLException
    {
        context.useAttributeSet(name, sourceNode, result);
    }
}
