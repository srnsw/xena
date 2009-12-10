// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.Variant;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.ExprContext;

/**
 *
 */
class ResultFragmentExpr implements VariantExpr
{
    final private Action action;
    final private Node stylesheetNode;
    final private ExtensionHandler extensionHandler;

    ResultFragmentExpr(Action action,
                       Node stylesheetNode,
                       ExtensionHandler extensionHandler)
    {
        this.action = action;
        this.stylesheetNode = stylesheetNode;
        this.extensionHandler = extensionHandler;
    }

    public Variant eval(Node node, ExprContext context)
    {
        return new ActionResultFragmentVariant(action,
                                               stylesheetNode,
                                               extensionHandler,
                                               node,
                                               ((ProcessContext)context).createMemento());
    }
}
