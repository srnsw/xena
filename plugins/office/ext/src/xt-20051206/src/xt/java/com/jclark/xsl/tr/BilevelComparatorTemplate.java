// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.util.Comparator;
import com.jclark.xsl.util.BilevelComparator;
import com.jclark.xsl.expr.ExprContext;

class BilevelComparatorTemplate implements ComparatorTemplate
{
    private final ComparatorTemplate t1;
    private final ComparatorTemplate t2;
    BilevelComparatorTemplate(ComparatorTemplate t1, ComparatorTemplate t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }

    public Comparator instantiate(Node node, ExprContext context) throws XSLException
    {
        return new BilevelComparator(t1.instantiate(node, context),
                                     t2.instantiate(node, context));
    }
}
