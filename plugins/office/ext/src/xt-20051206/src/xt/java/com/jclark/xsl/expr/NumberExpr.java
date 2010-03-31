// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public interface NumberExpr {
    double eval(Node node, ExprContext context) throws XSLException;
}
