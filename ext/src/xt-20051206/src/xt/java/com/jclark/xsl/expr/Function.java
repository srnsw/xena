// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * all functions have the method: "makeCallExpr( ... ) "
 */
interface Function 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr[] args, Node exprNode) 
	throws ParseException;
}
