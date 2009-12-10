// $Id$

package com.jclark.xsl.sax;

/**
 * maps between Java and IANA character encoding names
 */
public abstract class EncodingName 
{
    private static String[][] encodingMap = new String[][] {
        // IANA name, Java name
        { "US-ASCII", "ASCII" },
        { "ISO-8859-1", "ISO8859_1" },
        { "ISO-8859-2", "ISO8859_2" },
        { "ISO-8859-3", "ISO8859_3" },
        { "ISO-8859-4", "ISO8859_4" },
        { "ISO-8859-5", "ISO8859_5" },
        { "ISO-8859-6", "ISO8859_6" },
        { "ISO-8859-7", "ISO8859_7" },
        { "ISO-8859-8", "ISO8859_8" },
        { "ISO-8859-9", "ISO8859_9" },
        { "Big5", "Big5" },
        { "windows-1250", "Cp1250" },
        { "windows-1251", "Cp1251" },
        { "windows-1252", "Cp1252" },
        { "windows-1253", "Cp1253" },
        { "windows-1254", "Cp1254" },
        { "windows-1255", "Cp1255" },
        { "windows-1256", "Cp1256" },
        { "windows-1257", "Cp1257" },
        { "windows-1258", "Cp1258" },
        { "EUC-JP", "EUC_JP" },
        { "EUC-KR", "EUC_KR" },
        { "ISO-2022-CN", "ISO2022CN" },
        { "ISO-2022-JP", "ISO2022JP" },
        { "ISO-2022-KR", "ISO2022KR" },
        { "KOI8-R", "KOI8_R" },
        { "TIS-620", "MS874" },
        { "Shift_JIS", "SJIS" },
        { "UTF-8", "UTF8" },
        { "UTF-16", "Unicode" },
        { "UTF-16LE", "UnicodeLittle" },
        { "UTF-16BE", "UnicodeBig" },
    };

    static {
        // Workaround for some VMs (eg Microsoft's)
        // that use 8859_1 rather than ISO8859_1.
        try {
            "".getBytes("ISO8859_1");
        }
        catch (java.io.UnsupportedEncodingException e) {
            encodingMap[1][1] = "8859_1";
        }
    }

    /**
     * convert a Java character encoding name to its IANA equivalent
     *
     * @return null if there is no equivalent
     */
    public static String toIana(String encoding)
    {
        if (encoding == null)
            return null;
        for (int i = 0; i < encodingMap.length; i++)
            if (encoding.equalsIgnoreCase(encodingMap[i][1]))
                return encodingMap[i][0];
        return encoding;
    }

    /**
     * convert a IANA character encoding name to its Java equivalent
     *
     * @return null if there is no equivalent
     */
    public static String toJava(String encoding) 
    {
        if (encoding == null) {
            return null;
        }
        for (int i = 0; i < encodingMap.length; i++) {
            if (encoding.equalsIgnoreCase(encodingMap[i][0])
                || encoding.equalsIgnoreCase(encodingMap[i][1])) {
                return encodingMap[i][1];
            }
        }
        return encoding;
    }
}
