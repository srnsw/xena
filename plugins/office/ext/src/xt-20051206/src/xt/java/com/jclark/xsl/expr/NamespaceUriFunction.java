// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NamespaceUriFunction extends FunctionOpt1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr expr) throws ParseException {
        final NodeSetExpr nse = expr.makeNodeSetExpr();
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    node = nse.eval(node, context).next();
                    if (node != null) {
                        Name name = node.getName();
                        if (name != null) {
                            String ns = name.getNamespace();
                            if (ns != null) {
                                return ns;
                            }
                        }
                    }
                    return "";
                }
            };
    }
}
