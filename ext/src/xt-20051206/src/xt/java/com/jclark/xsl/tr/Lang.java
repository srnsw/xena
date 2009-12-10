// $Id$

package com.jclark.xsl.tr;

import java.util.Locale;

/**
 * Convert the value of an xml:lang attribute to a Locale. 
*/

class Lang
{
    /* lang may be null. */
    static Locale getLocale(String lang)
    {
        // FIXME handle non ISO 639 codes
        if (lang == null || lang.length() < 2 || lang.charAt(1) == '-') {
            return null;
        }
        return new Locale(lang.substring(0, 2), getCountryCode(lang));
    }

    static private String getCountryCode(String lang)
    {
        int len = lang.length();
        if (len < 5
            || (len > 5 && lang.charAt(5) != '-')
            || lang.charAt(4) == '-')
            return "";
        return lang.substring(3, 5);
    }
	
}
