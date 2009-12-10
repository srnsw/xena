// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * the parsed representation of a stylesheet component.
 *  When evaluated (or invoked) with a ProcessContext
 *  and context Node, generates something which is
 * sent to a result
 */
interface Action
{
    void invoke(ProcessContext context, Node sourceNode, 
                Result result) throws XSLException;
}
