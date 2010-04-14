// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class InheritPattern implements Pattern {
    private Pattern p;

    InheritPattern(Pattern p) {
        this.p = p;
    }

    public boolean matches(Node node, ExprContext context) throws XSLException {
        do {
            if (p.matches(node, context))
                return true;
            node = node.getParent();
        } while (node != null);
        return false;
    }
}
