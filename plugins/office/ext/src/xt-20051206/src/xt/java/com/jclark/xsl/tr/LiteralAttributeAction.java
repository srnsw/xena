// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class LiteralAttributeAction implements Action
{
    private Name name;
    private String value;

    LiteralAttributeAction(Name name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public void invoke(ProcessContext context, Node sourceNode,
                       Result result)
        throws XSLException
    {
        result.attribute(context.unaliasName(name), value);
    }
}
