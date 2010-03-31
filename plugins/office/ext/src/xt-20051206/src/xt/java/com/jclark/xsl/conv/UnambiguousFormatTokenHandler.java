// $Id$

package com.jclark.xsl.conv;

abstract class UnambiguousFormatTokenHandler implements NumberFormat, FormatTokenHandler
{
    public NumberFormat getFormat(String lang, String letterValue)
    {
        return this;
    }
}
