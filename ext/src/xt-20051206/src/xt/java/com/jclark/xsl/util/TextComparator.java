// $Id$

package com.jclark.xsl.util;

import java.util.Locale;
import java.text.Collator;

public class TextComparator implements Comparator 
{
    private Collator collator;
  
    public static final int UPPER_FIRST = 1;
    public static final int LOWER_FIRST = 2;

    public static Comparator create(Locale locale, int caseOrder) 
    {
        TextComparator cmp = new TextComparator(locale);
        // Might be better to handle this by diddling the rules
        // JDK does lower-first; it would be better not to assume this
        if ((caseOrder & UPPER_FIRST) == 0)
            return cmp;
        if (locale == null)
            locale = locale.getDefault();
        if (locale.getLanguage().equals("tr"))
            return new TurkishSwapCaseComparator(cmp);
        return new SwapCaseComparator(cmp);
    }

    private TextComparator(Locale locale) 
    {
        if (locale == null)
            collator = Collator.getInstance();
        else
            collator = Collator.getInstance(locale);
        collator.setStrength(Collator.TERTIARY);
    }

    public int compare(Object obj1, Object obj2) 
    {
        return collator.compare((String)obj1, (String)obj2);
    }

}

