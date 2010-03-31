// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public interface CloneableNodeIterator extends NodeIterator
{
    Object clone();
    // Bind variable references to the values, so that
    // the iterator is protected from mutatations in the
    // ExprContext.
    void bind() throws XSLException;
}
