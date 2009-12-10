// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NodeSetFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) 
    {
        final VariantExpr ve = e.makeVariantExpr();
        return new ConvertibleNodeSetExpr() 
            {
                public NodeIterator eval(Node node, ExprContext context)
                    throws XSLException 
                {
                    
                    Variant v = ve.eval(node, context);
                    Node nd = context.getTree(v);
                    if (nd != null) {
                        return new SingleNodeIterator(nd); 
                    }
                    return v.convertToNodeSet();
                }
            };
    }
}
