// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

abstract class NumericRelation extends Relation {
    boolean relateAtomic(Variant obj1, Variant obj2) throws XSLException {
        return relate(obj1.convertToNumber(), obj2.convertToNumber());
    }
    boolean relate(String s1, String s2) {
        return relate(Converter.toNumber(s1),
                      Converter.toNumber(s2));
    }
    boolean relate(boolean b1, boolean b2) {
        return relate(b1 ? 1.0 : 0.0,
                      b2 ? 1.0 : 0.0);
    }
}
