// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class NumberFunction extends FunctionOpt1 {
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException {
        return e.makeNumberExpr();
    }
}
