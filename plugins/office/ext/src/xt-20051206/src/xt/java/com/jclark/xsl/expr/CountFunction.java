// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * Represents the XPath 
 * Function: number count(node-set) 
 *
 *       The count function returns the number of nodes in
 *        the argument node-set.
 */
class CountFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException
    {
        final NodeSetExpr nse = e.makeNodeSetExpr();
        return new ConvertibleNumberExpr() 
            {
                public double eval(Node node, ExprContext context) 
                    throws XSLException
                {
                    return count(nse.eval(node, context));
                }
            };
    }

    static private final int count(NodeIterator iter) throws XSLException
    {
        int n = 0;
        while (iter.next() != null) {
            n++;
        }
        return n;
    }
}
