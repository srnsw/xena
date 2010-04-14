// $Id$

package com.jclark.xsl.conv;

import java.net.URL;
import java.util.Hashtable;

/**
 *
 */
public class NumberListFormat implements Cloneable
{

    static private NumberFormat defaultFormat = 
        new DecimalNumberFormat('0', 1);

    static private Hashtable formatTokenHandlerTable = new Hashtable();

    static {
        NumberFormat format = 
            new AlphabetNumberFormat("abcdefghijklmnopqrstuvwxyz");
        AlphabetFormatTokenHandler handler = new AlphabetFormatTokenHandler(format);
        formatTokenHandlerTable.put("a", handler);
        format = 
            new AlphabetNumberFormat("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        handler = 
            new AlphabetFormatTokenHandler(format);
        formatTokenHandlerTable.put("A", handler);
        formatTokenHandlerTable.put("i", new RomanNumberFormat("mdclxvi"));
        formatTokenHandlerTable.put("I", new RomanNumberFormat("MDCLXVI"));
    }

    private NumberFormat[] formats = null;
    private String[] formatTokens = new String[] { "1" };
    private String prefix = "";
    private String suffix = "";
    // i-th element is separator between i-th and i+1-th number
    private String[] separators = new String[] { "." };
    private int groupingSize = 0;
    private String groupingSeparator = null;
    private String lang = null;
    private String letterValue = null;

    public static
        void setFormatTokenHandler(String formatToken,
                                   FormatTokenHandler handler)
    {
        formatTokenHandlerTable.put(formatToken, handler);
    }

    public void setFormat(String format)
    {
        formats = null;
        int n = 0;
        for (int i = 0; i < format.length(); i++) {
            if (isAlnum(format.charAt(i))
                && (i == 0 || !isAlnum(format.charAt(i - 1)))) {
                n++;
            }
        }
        if (n == 0) {
            formatTokens = new String[1];
            formatTokens[0] = "1";
            prefix = "";
        } else {
            formatTokens = new String[n];
        }
        if (n <= 1) {
            separators = new String[1];
            separators[0] = ".";
        }
        else {
            separators = new String[n - 1];
        }
        int fi = 0;
        for (int i = 0; i < n; i++) {
            int sepStart = fi;
            while (!isAlnum(format.charAt(fi))) {
                fi++;
            }
            if (i == 0) {
                prefix = format.substring(0, fi);
            } else {
                separators[i - 1] = format.substring(sepStart, fi);
            }
            int numberStart = fi++;
            while (fi < format.length() && isAlnum(format.charAt(fi))) {
                fi++;
            }
            formatTokens[i] = format.substring(numberStart, fi);
        }
        suffix = format.substring(fi);
    }

    static private boolean isAlnum(char c) 
    {
        switch (Character.getType(c)) {
        case Character.UPPERCASE_LETTER:
        case Character.LOWERCASE_LETTER:
        case Character.TITLECASE_LETTER:
        case Character.MODIFIER_LETTER:
        case Character.OTHER_LETTER:
        case Character.DECIMAL_DIGIT_NUMBER:
        case Character.LETTER_NUMBER:
        case Character.OTHER_NUMBER:
            return true;
        }
        return false;
    }

    public void setLang(String lang) 
    {
        formats = null;
        this.lang = lang;
    }

    public void setLetterValue(String letterValue) 
    {
        this.letterValue = letterValue;
    }

    public void setGroupingSeparator(String sep) 
    {
        if (sep == null || sep.length() == 0) {
            groupingSeparator = null;
        } else {
            groupingSeparator = sep;
        }
    }

    public void setGroupingSize(int n) 
    {
        groupingSize = n;
    }

    public String getPrefix(int i) 
    {
        if (i == 0) {
            return prefix;
        }
        i -= 1;
        if (i < separators.length) {
            return separators[i];
        }
        return separators[separators.length - 1];
    }

    public String formatNumber(int i, int n) 
    {
        if (n < 0) {
            throw new IllegalArgumentException("cannot format negative number");
        }
        return group(getFormat(i).format(n));
    }

    public String getSuffix() 
    {
        return suffix;
    }

    private NumberFormat getFormat(int i) 
    {
        if (formats == null) {
            formats = new NumberFormat[formatTokens.length];
        }
        if (i >= formats.length) {
            i = formats.length - 1;
        }
        if (formats[i] == null) {
            formats[i] = findFormat(formatTokens[i]);
        }
        return formats[i];
    }

    private NumberFormat findFormat(String formatToken) 
    {
        NumberFormat f = findDecimalFormat(formatToken);
        if (f != null) {
            return f;
        }
        FormatTokenHandler handler = 
            (FormatTokenHandler)formatTokenHandlerTable.get(formatToken);
        if (handler != null) {
            f = handler.getFormat(lang, letterValue);
            if (f != null) {
                return f;
            }
        }
        return defaultFormat;
    }

    private NumberFormat findDecimalFormat(String format) 
    {
        char digitOne = format.charAt(format.length() - 1);
        if (!Character.isDigit(digitOne) || 
            Character.digit(digitOne, 10) != 1) {
            return null;
        }

        for (int i = 0; i < format.length() - 1; i++) {
            if (format.charAt(i) + 1 != digitOne) {
                return null;
            }
        }
        return new DecimalNumberFormat((char)(digitOne - 1), format.length());
    }

    private String group(String number) 
    {
        if (groupingSeparator == null ||
            groupingSize <= 0 || 
            number.length() <= groupingSize) {
            return number;
        }
        char buf[] = new char[number.length() + 
                              ((number.length() - 1)/groupingSize) * 
                              groupingSeparator.length()];
        int j = 0;
        for (int i = 0; i < number.length(); i++) {
            if (i > 0 && (number.length() - i) % groupingSize == 0) {
                for (int k = 0; k < groupingSeparator.length(); k++) {
                    buf[j++] = groupingSeparator.charAt(k);
                }
            }
            buf[j++] = number.charAt(i);
        }
        return new String(buf);
    }

    public Object clone() 
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) { 
            throw new InternalError();
        }
    }

}
