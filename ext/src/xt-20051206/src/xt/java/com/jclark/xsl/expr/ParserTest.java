// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class ParserTest {
    static public void main(String args[]) throws XSLException {
        ExprParser.parseStringExpr(null, args[0], new EmptyVariableSet());
    }
}
