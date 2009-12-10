// $Id$

package com.jclark.xsl.expr;

class GreaterThanRelation extends NumericRelation {
    boolean relate(double d1, double d2) {
        return d1 > d2;
    }
}
