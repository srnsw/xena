// $Id$

package com.jclark.xsl.conv;

class RomanNumberFormat extends UnambiguousFormatTokenHandler
{
    private String letters;

    RomanNumberFormat(String letters)
    {
        this.letters = letters;
    }

    public String format(int n)
    {
        if (n == 0)
            return "0";
        if (n > 5000)
            return Integer.toString(n);
        StringBuffer result = new StringBuffer();
        while (n >= 1000) {
            result.append(letters.charAt(0));
            n -= 1000;
        }
        int letterBase = 0;
        for (int i = 100; i > 0; i /= 10, letterBase += 2) {
            int q = n / i;
            n -= q * i;
            switch (q) {
            case 1:
                result.append(letters.charAt(letterBase + 2));
                break;
            case 2:
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                break;
            case 3:
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                break;
            case 4:
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 1));
                break;
            case 5:
                result.append(letters.charAt(letterBase + 1));
                break;
            case 6:
                result.append(letters.charAt(letterBase + 1));
                result.append(letters.charAt(letterBase + 2));
                break;
            case 7:
                result.append(letters.charAt(letterBase + 1));
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                break;
            case 8:
                result.append(letters.charAt(letterBase + 1));
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 2));
                break;
            case 9:
                result.append(letters.charAt(letterBase + 2));
                result.append(letters.charAt(letterBase + 0));
                break;
            }
        }
        return result.toString();
    }
}
