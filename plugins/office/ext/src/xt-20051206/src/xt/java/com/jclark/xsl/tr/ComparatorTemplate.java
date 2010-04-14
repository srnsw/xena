// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.util.Comparator;
import com.jclark.xsl.expr.ExprContext;

/**
 *
 */
interface ComparatorTemplate
{
    Comparator instantiate(Node node, ExprContext context) throws XSLException;
}
