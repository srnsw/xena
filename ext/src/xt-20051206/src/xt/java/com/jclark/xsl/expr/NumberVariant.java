// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class NumberVariant extends VariantBase {
    private final double num;

    public NumberVariant(double num) {
        this.num = num;
    }

    public String convertToString() {
        return Converter.toString(num);
    }

    public boolean convertToBoolean() {
        return Converter.toBoolean(num);
    }

    public double convertToNumber() {
        return num;
    }

    public boolean convertToPredicate(ExprContext context) throws XSLException {
        return Converter.positionToBoolean(num, context);
    }

    public Object convertToObject() {
        return new Double(num);
    }

    public boolean isNumber() { return true; }
}
