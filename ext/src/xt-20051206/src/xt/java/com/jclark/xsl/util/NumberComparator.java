// $Id$

package com.jclark.xsl.util;

public class NumberComparator implements Comparator
{
    public int compare(Object obj1, Object obj2)
    {
        double res = getValue(obj1) - getValue(obj2);
        if (res < 0.0)
            return -1;
        if (res > 0.0)
            return 1;
        return 0;
    }
  
    public double getValue(Object obj)
    {
        try {
            return Double.valueOf(((String)obj).trim()).doubleValue();
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
