// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * character data
 */
class CharsAction implements Action
{
    private String chars;
    CharsAction(String chars)
    {
        this.chars = chars;
    }

    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) throws XSLException
    {
        result.characters(chars);
    }
}

