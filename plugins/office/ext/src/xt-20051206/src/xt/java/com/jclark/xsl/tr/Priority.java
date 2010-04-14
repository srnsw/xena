// $Id$

package com.jclark.xsl.tr;

final class Priority
{
    private final double n;
    static private final Priority defaultCache[] = new Priority[]{
        new Priority(-0.5),
        new Priority(-0.25),
        new Priority(0.0),
        new Priority(0.5)
            };

    private Priority(double n)
    {
        this.n = n;
    }

    // -2 means -0.5; -1 means -0.25; 0 means 0; 1 means 0.5
    public static Priority createDefault(int p)
    {
        if (p < -2 || p > 1)
            throw new IllegalArgumentException("bad default");
        return defaultCache[p + 2];
    }

    public static Priority create(String s) throws NumberFormatException
    {
        if (s == null)
            return null;
        return new Priority(Double.valueOf(s).doubleValue());
    }
  
    public int compareTo(Priority p)
    {
        if (n == p.n)
            return 0;
        return n < p.n ? -1 : 1;
    }

}
