// $Id$

package com.jclark.xsl.expr;

class TokenizerTest {
    static public void main(String args[]) throws ParseException {
        String[] tokenNames = new String[] {
            "EOF",
            "QNAME",
            "STAR",
            "NAME_COLON_STAR",
            "AT",
            "DOT",
            "DOT_DOT",
            "COMMENT_LPAR",
            "PI_LPAR",
            "TEXT_LPAR",
            "NODE_LPAR",
            "MULTIPLY",
            "LPAR",
            "RPAR",
            "LSQB",
            "RSQB",
            "LITERAL",
            "NUMBER",
            "AXIS_LPAR",
            "FUNCTION_LPAR",
            "CNAME_LPAR",
            "VARIABLE_REF",
            "SLASH",
            "SLASH_SLASH",
            "VBAR",
            "COMMA",
            "PLUS",
            "MINUS",
            "EQUALS",
            "GT",
            "LT",
            "GTE",
            "LTE",
            "AND",
            "OR",
            "MOD",
            "DIV",
            "QUO"
        };
        ExprTokenizer t = new ExprTokenizer(args[0]);
        do {
            t.next();
            System.err.print(tokenNames[t.currentToken]);
            if (t.currentTokenValue != null) {
                System.err.println("=" + t.currentTokenValue);
            } else {
                System.err.println();
            }
        } while (t.currentToken != ExprTokenizer.TOK_EOF);
    }
}
