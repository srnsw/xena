// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;
import com.jclark.xsl.conv.NumberListFormat;

/**
 *
 */
class NumberListFormatTemplate
{
    private NumberListFormat format = new NumberListFormat();
    private StringExpr formatExpr = null;

    NumberListFormat instantiate(ProcessContext context,
                                 Node sourceNode) 
        throws XSLException
    {
        if (formatExpr != null) {
            NumberListFormat tem = (NumberListFormat)format.clone();
            tem.setFormat(formatExpr.eval(sourceNode, context));
            return tem;
        }
        return format;
    }

    void setFormat(StringExpr expr)
    {
        formatExpr = expr;
    }

    void setFormat(String s) 
    {
        formatExpr = null;
        format.setFormat(s);
    }

    void setLang(String s)
    {
        format.setLang(s);
    }

    void setLetterValue(String letterValue)
    {
        format.setLetterValue(letterValue);
    }

    void setGroupingSeparator(String sep)
    {
        format.setGroupingSeparator(sep);
    }

    void setGroupingSize(int n)
    {
        format.setGroupingSize(n);
    }
}
