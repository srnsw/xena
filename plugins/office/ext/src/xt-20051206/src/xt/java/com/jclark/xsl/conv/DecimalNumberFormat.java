// $Id$

package com.jclark.xsl.conv;

/**
 *
 */
class DecimalNumberFormat implements NumberFormat
{
    private char digitZero;
    private int minDigits;

    DecimalNumberFormat(char digitZero, int minDigits)
    {
        this.digitZero = digitZero;
        this.minDigits = minDigits;
    }

    public String format(int n)
    {
        String s = Integer.toString(n);
        if (digitZero == '0' && s.length() >= minDigits) {
            return s;
        }
        StringBuffer buf = new StringBuffer();
        if (s.length() < minDigits) {
            int i = minDigits - s.length();
            while (i-- != 0) {
                buf.append(digitZero);
            }
        }
        for (int i = 0; i < s.length(); i++) {
            buf.append((char)(digitZero + (s.charAt(i) - '0')));
        }
        return buf.toString();
    }
}
