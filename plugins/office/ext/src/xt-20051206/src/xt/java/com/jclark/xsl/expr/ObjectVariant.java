// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class ObjectVariant extends VariantBase 
{
    private final Object obj;

    ObjectVariant(Object obj)
    {
        this.obj = obj;
    }

    public Object convertToObject() 
    {
        return obj;
    }

    public String convertToString() 
    {
        if (obj == null) {
            return "";
	}
        return obj.toString();
    }

    public boolean convertToBoolean() 
    {
        return obj != null;
    }
}
