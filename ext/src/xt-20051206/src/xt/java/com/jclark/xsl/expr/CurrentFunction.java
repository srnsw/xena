// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * Represents the XSLT Function: node-set current() 
 *
 * The current function returns a node-set that has the
 *  current node as its only member. For an outermost
 * expression (an expression not occurring within
 *  another expression), the current node is always the same
 *        as the context node. Thus,
 */
class CurrentFunction extends Function0
{
    ConvertibleExpr makeCallExpr()
    {
        return new ConvertibleNodeSetExpr()
            {
                public NodeIterator eval(Node node,
                                         ExprContext context) 
                    throws XSLException
                {
                    return new SingleNodeIterator(context.getCurrent(node));
                }
            };
    }
}
