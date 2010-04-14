// $Id$

package com.jclark.xsl.conv;

/**
 *
 */
class AlphabetNumberFormat implements NumberFormat
{
    private String letters;

    AlphabetNumberFormat(String letters)
    {
        this.letters = letters;
    }

    public String format(int n)
    {
        if (n == 0) {
            return "0";
        }
        StringBuffer result = new StringBuffer();
        do {
            n--;
            int r = n % letters.length();
            n -= r;
            n /= 26;  // modulo 26
            result.append(letters.charAt(r));
        } while (n > 0);

        return result.reverse().toString();
    }
}
