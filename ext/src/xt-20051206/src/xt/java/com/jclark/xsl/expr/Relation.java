// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

abstract class Relation 
{
    abstract boolean relate(String s1, String s2);
    abstract boolean relate(boolean b1, boolean b2);
    abstract boolean relate(double d1, double d2);
  
    boolean relate(NodeIterator iter1, NodeIterator iter2) throws XSLException 
    {
        if (!(iter2 instanceof CloneableNodeIterator))
            iter2 = new CloneableNodeIteratorImpl(iter2);
        for (;;) {
            Node node1 = iter1.next();
            if (node1 == null)
                break;
            String s1 = Converter.toString(node1);
            NodeIterator tem = (NodeIterator)((CloneableNodeIterator)iter2).clone();
            for (;;) {
                Node node2 = tem.next();
                if (node2 == null)
                    break;
                if (relate(s1, Converter.toString(node2)))
                    return true;
            }
        }
        return false;
    }

    boolean relate(NodeIterator iter, boolean b) throws XSLException 
    {
        return relate(iter.next() != null, b);
    }

    boolean relate(NodeIterator iter, double d) throws XSLException 
    {
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            if (relate(Converter.toNumber(Converter.toString(node)), d))
                return true;
        }
        return false;
    }

    boolean relate(NodeIterator iter, String s) throws XSLException
    {
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            if (relate(Converter.toString(node), s))
                return true;
        }
        return false;
    }

    boolean relate(boolean b, NodeIterator iter) throws XSLException 
    {
        return relate(b, iter.next() != null);
    }

    boolean relate(double d, NodeIterator iter) throws XSLException 
    {
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            if (relate(d, Converter.toNumber(Converter.toString(node))))
                return true;
        }
        return false;
    }

    boolean relate(String s, NodeIterator iter) throws XSLException
    {
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            if (relate(s, Converter.toString(node)))
                return true;
        }
        return false;
    }

    boolean relate(Variant obj1, Variant obj2) throws XSLException
    {
        if (obj1.isNodeSet()) {
            if (obj2.isNodeSet())
                return relate(obj1.convertToNodeSet(),
                              obj2.convertToNodeSet());
            if (obj2.isNumber())
                return relate(obj1.convertToNodeSet(),
                              obj2.convertToNumber());
            if (obj2.isBoolean())
                return relate(obj1.convertToNodeSet(),
                              obj2.convertToBoolean());
            return relate(obj1.convertToNodeSet(),
                          obj2.convertToString());
        }
        if (obj2.isNodeSet()) {
            if (obj1.isNumber())
                return relate(obj1.convertToNumber(),
                              obj2.convertToNodeSet());
		      
            if (obj1.isBoolean())
                return relate(obj1.convertToBoolean(),
                              obj2.convertToNodeSet());
            return relate(obj1.convertToString(),
                          obj2.convertToNodeSet());
        }
        return relateAtomic(obj1, obj2);
    }

    boolean relateAtomic(Variant obj1, Variant obj2) throws XSLException 
    {
        if (obj1.isBoolean() || obj2.isBoolean())
            return relate(obj1.convertToBoolean(), obj2.convertToBoolean());
        if (obj1.isNumber() || obj2.isNumber())
            return relate(obj1.convertToNumber(), obj2.convertToNumber());
        return relate(obj1.convertToString(), obj2.convertToString());
    }
}
