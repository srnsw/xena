// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * James Clark's extension function: node-set xt:difference(node-set, node-set)
 */
class DifferenceFunction extends Function2 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e1, 
                                 ConvertibleExpr e2)
        throws ParseException 
    {
        final NodeSetExpr nse1 = e1.makeNodeSetExpr();
        final NodeSetExpr nse2 = e2.makeNodeSetExpr();

        return new ConvertibleNodeSetExpr() 
            {
                public NodeIterator eval(Node node, 
                                         ExprContext context) 
                    throws XSLException 
                {
                    return new DifferenceNodeIterator(nse1.eval(node, context),
                                                      nse2.eval(node, context));
                }
            };
    }
}
