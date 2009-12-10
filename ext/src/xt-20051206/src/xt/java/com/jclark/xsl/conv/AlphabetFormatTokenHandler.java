// $Id$

package com.jclark.xsl.conv;

import java.util.Hashtable;

/**
 *
 */
class AlphabetFormatTokenHandler implements FormatTokenHandler
{
    private Hashtable langTable = new Hashtable();
    private NumberFormat defaultFormat;

    AlphabetFormatTokenHandler(NumberFormat defaultFormat)
    {
        this.defaultFormat = defaultFormat;
    }

    void setLangNumberFormat(String lang, NumberFormat format)
    {
        langTable.put(lang, format);
    }

    public NumberFormat getFormat(String lang, String letterValue)
    {
        if (lang == null) {
            return defaultFormat;
        }
        NumberFormat format = 
            (NumberFormat)langTable.get(lang.toLowerCase());

        if (format != null) {
            return format;
        }
        int i = lang.lastIndexOf('-');
        if (i > 1) {
            return getFormat(lang.substring(0, i), letterValue);
        }
        return defaultFormat;
    }
}
