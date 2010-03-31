// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

class LangFunction extends Function1 
{
    ConvertibleExpr makeCallExpr(ConvertibleExpr e) 
        throws ParseException 
    {
        final StringExpr se = e.makeStringExpr();
        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, ExprContext context) throws XSLException {
                    return lang(node, se.eval(node, context));
                }
            };
    }
    
    private static boolean lang(Node node, String lang) 
    {
        Name XML_LANG =
            node.getNamespacePrefixMap().getNameTable().createName("xml:lang",
                                                                   Name.XML_NAMESPACE);
        while (node != null) {
            String nodeLang = node.getAttributeValue(XML_LANG);
            if (nodeLang != null)
                return isSubLanguage(lang, nodeLang);
            node = node.getParent();
        }
        return false;
    }

    private static boolean isSubLanguage(String lang1, String lang2) 
    {
        int len1 = lang1.length();
        int len2 = lang2.length();
        if (len1 > len2)
            return false;
        if (len1 < len2 && lang2.charAt(len1) != '-')
            return false;
        for (int i = 0; i < len1; i++) {
            char c1 = lang1.charAt(i);
            char c2 = lang2.charAt(i);
            switch ((int)c1 - (int)c2) {
            case 0:
            case 'a' - 'A':
            case 'A' - 'a':
                break;
            default:
                return false;
            }
        }
        return true;
    }
}
