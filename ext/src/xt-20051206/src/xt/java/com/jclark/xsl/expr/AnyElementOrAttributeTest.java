// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class AnyElementOrAttributeTest implements Pattern
{
    public boolean matches(Node node, ExprContext context)
    {
        switch (node.getType()) {
        case Node.ELEMENT:
        case Node.ATTRIBUTE:
            return true;
        }
        return false;
    }
}
