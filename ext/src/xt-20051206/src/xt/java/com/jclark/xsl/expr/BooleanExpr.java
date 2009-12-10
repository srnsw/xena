// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public interface BooleanExpr
{
    boolean eval(Node node, ExprContext context) throws XSLException;
}
