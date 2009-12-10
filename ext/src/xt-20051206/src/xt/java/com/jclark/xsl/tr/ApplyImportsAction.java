// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 *
 */
class ApplyImportsAction implements Action
{
    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        context.applyImports(sourceNode, result);
    }
}
