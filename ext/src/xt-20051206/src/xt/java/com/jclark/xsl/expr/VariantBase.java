// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public abstract class VariantBase implements Variant 
{
    public Variant makePermanent() throws XSLException 
    { 
        return this; 
    }

    public NodeIterator convertToNodeSet() throws XSLException 
    {
        throw new XSLException("cannot convert to node-set");
    }

    public double convertToNumber() throws XSLException 
    {
        return Converter.toNumber(convertToString());
    }

    public boolean convertToPredicate(ExprContext context) throws XSLException 
    {
        return convertToBoolean();
    }

    public boolean isBoolean() { return false; }
    public boolean isNumber() { return false; }
    public boolean isString() { return false; }
    public boolean isNodeSet() { return false; }

    static public Variant create(Object obj) {
        if (obj instanceof String)
            return new StringVariant((String)obj);
        if (obj instanceof Number)
            return new NumberVariant(((Number)obj).doubleValue());
        if (obj instanceof Boolean)
            return new BooleanVariant(((Boolean)obj).booleanValue());
        if (obj instanceof NodeIterator)
            return new NodeSetVariant((NodeIterator)obj);
        if (obj instanceof Node)
            return new NodeSetVariant(new SingleNodeIterator((Node)obj));
        if (obj instanceof Variant)
            return (Variant)obj;
        return new ObjectVariant(obj);
    }

    public Node getBaseNode() {
        return null;
    }
}
