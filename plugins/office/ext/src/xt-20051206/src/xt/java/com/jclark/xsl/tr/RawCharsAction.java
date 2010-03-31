// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class RawCharsAction implements Action
{
    private String chars;

    RawCharsAction(String chars)
    {
        this.chars = chars;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) 
        throws XSLException
    {
        result.rawCharacters(chars);
    }
}

