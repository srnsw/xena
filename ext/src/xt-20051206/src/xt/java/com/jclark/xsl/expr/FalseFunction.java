// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * a function of 0 arguments that returns false
 */
class FalseFunction extends Function0 
{
    ConvertibleExpr makeCallExpr() 
    {
        return new FalseExpr();
    }
}
