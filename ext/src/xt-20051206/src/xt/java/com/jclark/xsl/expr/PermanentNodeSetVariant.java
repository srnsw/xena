// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class PermanentNodeSetVariant extends VariantBase 
{
    private final CloneableNodeIterator iter;

    PermanentNodeSetVariant(NodeIterator iter) throws XSLException 
    {
        if (iter instanceof CloneableNodeIterator) {
            this.iter = (CloneableNodeIterator)iter;
        } else {
            this.iter = new CloneableNodeIteratorImpl(iter);
	}
        this.iter.bind();
    }

    public NodeIterator convertToNodeSet() 
    {
        return (NodeIterator)iter.clone();
    }

    public Object convertToObject() 
    {
        return iter.clone();
    }

    public String convertToString() throws XSLException 
    {
        return Converter.toString(convertToNodeSet());
    }

    public boolean convertToBoolean() throws XSLException 
    {
        return Converter.toBoolean(convertToNodeSet());
    }

    public double convertToNumber() throws XSLException 
    {
        return Converter.toNumber(convertToNodeSet());
    }

    public boolean isNodeSet() { return true; }
}
