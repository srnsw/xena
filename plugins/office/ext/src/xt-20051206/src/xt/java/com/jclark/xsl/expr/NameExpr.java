// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public interface NameExpr {
    Name eval(Node node, ExprContext context) throws XSLException;
}
