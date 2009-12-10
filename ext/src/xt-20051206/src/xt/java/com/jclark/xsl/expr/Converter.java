// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
final class Converter
{
    private Converter() { }

    static double toNumber(String str)
    {
        try {
            return Double.valueOf(str).doubleValue();
        }
        catch (NumberFormatException e)
            {
                return Double.NaN;
            }
    }

    static double toNumber(NodeIterator iter) throws XSLException
    {
        return toNumber(toString(iter));
    }

    static double toNumber(boolean b)
    {
        return b ? 1.0 : 0.0;
    }

    static boolean toBoolean(String str)
    {
        return str.length() != 0;
    }
    static boolean toBoolean(double num)
    {
        return num != 0.0 && num == num; // careful with NaN
    }
    static boolean toBoolean(NodeIterator iter) throws XSLException
    {
        return iter.next() != null;
    }

    static String toString(NodeIterator iter) throws XSLException
    {
        return toString(iter.next());
    }

    static String toString(Node node) throws XSLException
    {
        if (node == null)
            return "";
        String value = node.getData();
        if (value != null)
            return value;
        StringBuffer buf = new StringBuffer();
        addContent(node, buf);
        return buf.toString();
    }

    private static void addContent(Node node, StringBuffer result) throws XSLException
    {
        switch (node.getType()) {
        case Node.TEXT:
            result.append(node.getData());
            break;
        case Node.ELEMENT:
        case Node.ROOT:
            for (NodeIterator iter = node.getChildren();;) {
                node = iter.next();
                if (node == null)
                    break;
                addContent(node, result);
            }
        }
    }

    static String toString(double num)
    {
        if (!Double.isInfinite(num)
            && (num >= (double)(1L << 53)
                || -num >= (double)(1L << 53)))
            return new java.math.BigDecimal(num).toString();
        String s = Double.toString(num);
        int len = s.length();
        if (s.charAt(len - 2) == '.' && s.charAt(len - 1) == '0') {
            s = s.substring(0, len - 2);
            if (s.equals("-0"))
                return "0";
            return s;
        }
        int e = s.indexOf('E');
        if (e < 0)
            return s;
        int exp = Integer.parseInt(s.substring(e + 1));
        String sign;
        if (s.charAt(0) == '-') {
            sign = "-";
            s = s.substring(1);
            --e;
        }
        else
            sign = "";
        int nDigits = e - 2;
        if (exp >= nDigits) {
            return sign + s.substring(0, 1) + s.substring(2, e) +
		zeros(exp - nDigits);
	}
        if (exp > 0) {
            return sign + s.substring(0, 1) + s.substring(2, 2 + exp) +
		"." + s.substring(2 + exp, e);
	}
        return sign + "0." + zeros(-1 - exp) + s.substring(0, 1) +
	    s.substring(2, e);
    }
    
    static private String zeros(int n) 
    {
        char[] buf = new char[n];
        for (int i = 0; i < n; i++)
            buf[i] = '0';
        return new String(buf);
    }

    static String toString(boolean b) 
    {
        return b ? "true" : "false";
    }

    static boolean positionToBoolean(double d, ExprContext context) 
	throws XSLException 
    {
        return context.getPosition() == d;
    }
}
