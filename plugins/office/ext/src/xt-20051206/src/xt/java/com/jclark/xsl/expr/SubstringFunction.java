// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class SubstringFunction implements Function {
    public ConvertibleExpr makeCallExpr(ConvertibleExpr[] args,
                                        Node exprNode) throws ParseException {
        if (args.length < 2 || args.length > 3)
            throw new ParseException("expected 2 or 3 arguments");
        final StringExpr se = args[0].makeStringExpr();
        final NumberExpr ne1 = args[1].makeNumberExpr();
        final NumberExpr ne2 = (args.length == 2
                                ? new NumberConstantExpr(1.0/0.0)
                                    : args[2].makeNumberExpr());
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return substring(se.eval(node, context),
                                     ne1.eval(node, context),
                                     ne2.eval(node, context));
                }
            };
    }
  
    static final private String substring(String str, double start, double len) {
        start = Math.floor(start + 0.5);
        double end = start + Math.floor(len + 0.5);
        int strLen = str.length();
        int pos = 1;
        int firstIndex = -1;
        int lastIndex = -1;
        for (int i = 0; i < strLen; i++, pos++) {
            if (pos >= start && pos < end) {
                if (firstIndex < 0)
                    firstIndex = i;
                if (isLowSurrogate(str.charAt(i)))
                    ++i;
                lastIndex = i;
            }
            else if (isLowSurrogate(str.charAt(i)))
                ++i;
        }
        if (firstIndex >= 0)
            return str.substring(firstIndex, lastIndex + 1);
        return "";
    }

    private final static boolean isLowSurrogate(char c) {
        return (c & 0xFC00) == 0xD800;
    }

}
