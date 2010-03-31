// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

abstract class AxisExpr extends ConvertibleNodeSetExpr
{
    ConvertibleNodeSetExpr makeFilterExpr(ConvertibleNodeSetExpr expr,
                                          BooleanExpr predicate) {
        return new FilterExpr(expr, predicate);
    }
    ConvertibleNodeSetExpr makeDocumentOrderExpr(ConvertibleNodeSetExpr expr) {
        return expr;
    }
}
