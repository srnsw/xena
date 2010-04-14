// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class TrueFunction extends Function0 {
    ConvertibleExpr makeCallExpr() {
        return new TrueExpr();
    }
}
