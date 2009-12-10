// 

package com.jclark.xsl.tr;

import com.jclark.xsl.expr.VariantExpr;

/**
 * the expression to evaluate for obraining a Variable's result,
 * and the fact of whether it is a parameter
 */
class VariableInfo 
{
    private final boolean _isParam;
    private final VariantExpr _expr;

    VariableInfo(VariantExpr expr, boolean isParam) 
    {
        this._isParam = isParam;
        this._expr = expr;
    }

    boolean isParam()
    {
        return _isParam;
    }

    VariantExpr getExpr()
    {
        return _expr;
    }
}
