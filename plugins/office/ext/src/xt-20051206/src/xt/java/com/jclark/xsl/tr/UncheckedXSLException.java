// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.XSLException;

class UncheckedXSLException extends RuntimeException
{
    private XSLException e;

    UncheckedXSLException(XSLException e)
    { this.e = e; }

    XSLException getXSLException()
    { return e; }
}
