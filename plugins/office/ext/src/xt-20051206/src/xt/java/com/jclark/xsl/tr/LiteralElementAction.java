// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class LiteralElementAction implements Action
{
    private Name name;
    private NamespacePrefixMap nsMap;
    private Action content;

    LiteralElementAction(Name name, NamespacePrefixMap nsMap, Action content)
    {
        this.name = name;
        this.nsMap = nsMap;
        this.content = content;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, Node sourceNode, Result result)
        throws XSLException
    {
        Name unaliasedName = context.unaliasName(name);
        result.startElement(unaliasedName,
                            context.unaliasNamespacePrefixMap(nsMap));
        if (content != null) {
            content.invoke(context, sourceNode, result);
        }
        result.endElement(unaliasedName);
    }

}
