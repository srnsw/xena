// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
public interface VariantExpr 
{
    Variant eval(Node node, ExprContext context) throws XSLException;
}
