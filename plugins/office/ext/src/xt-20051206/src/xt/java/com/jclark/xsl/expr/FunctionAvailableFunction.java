// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * implements XSLT function-available("qname") function
 * XSLT sections 14.2 and 15
 */
class FunctionAvailableFunction implements Function 
{
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode)
        throws ParseException 
    {
        if (e.length != 1) {
            throw new ParseException("expected one argument");
	}
        final StringExpr se = e[0].makeStringExpr();
        final NamespacePrefixMap prefixMap = exprNode.getNamespacePrefixMap();
        return new ConvertibleBooleanExpr() 
	    {
                public boolean eval(Node node, ExprContext context) 
		    throws XSLException 
		{
                    return ExprParser.functionAvailable(prefixMap.expandAttributeName(se.eval(node, context), node), context);
                }
            };
    }
}
