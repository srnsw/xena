// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * lexical analyser for XPath 1.0
 * @see http://www.w3c.org/TR/xpath
 */
class ExprTokenizer 
{
    //
    // The tokens
    //

    // end of string being tokenized
    static final int TOK_EOF = 0;

    // a qName
    static final int TOK_QNAME = TOK_EOF + 1;

    // "*" (wildcard) ?
    static final int TOK_STAR = TOK_QNAME + 1;

    // "foo:*" ?
    static final int TOK_NAME_COLON_STAR = TOK_STAR + 1;

    // "@" (attribute axis specifier)
    static final int TOK_AT = TOK_NAME_COLON_STAR + 1;

    // "." (node self)
    static final int TOK_DOT = TOK_AT + 1;

    // ".." parent axis
    static final int TOK_DOT_DOT = TOK_DOT + 1;

    // "comment("
    static final int TOK_COMMENT_LPAR = TOK_DOT_DOT + 1;

    // "processing-instruction(" ??
    static final int TOK_PROCESSING_INSTRUCTION_LPAR = TOK_COMMENT_LPAR + 1;

    // "text("
    static final int TOK_TEXT_LPAR = TOK_PROCESSING_INSTRUCTION_LPAR + 1;

    // "node("
    static final int TOK_NODE_LPAR = TOK_TEXT_LPAR + 1;

    // "*" ?? same as star?
    static final int TOK_MULTIPLY = TOK_NODE_LPAR + 1;

    // "("
    static final int TOK_LPAR = TOK_MULTIPLY + 1;

    // ")"
    static final int TOK_RPAR = TOK_LPAR + 1;

    // "["
    static final int TOK_LSQB = TOK_RPAR + 1;

    // "]"
    static final int TOK_RSQB = TOK_LSQB + 1;

    //  'xxx ' | " xxx "
    static final int TOK_LITERAL = TOK_RSQB + 1;

    // [0-9].?[0-9]*
    static final int TOK_NUMBER = TOK_LITERAL + 1;

    // e.g.  "ancestor-or-self::"
    static final int TOK_AXIS = TOK_NUMBER + 1;

    // "foo("
    static final int TOK_FUNCTION_LPAR = TOK_AXIS + 1;

    // ??  maybe: "colonized name(" ??
    static final int TOK_CNAME_LPAR = TOK_FUNCTION_LPAR + 1;

    // "$foo"
    static final int TOK_VARIABLE_REF = TOK_CNAME_LPAR + 1;

    // "/"
    static final int TOK_SLASH = TOK_VARIABLE_REF + 1;

    // "//"  
    static final int TOK_SLASH_SLASH = TOK_SLASH + 1;

    // "|"
    static final int TOK_VBAR = TOK_SLASH_SLASH + 1;

    // ","
    static final int TOK_COMMA = TOK_VBAR + 1;

    // "+"
    static final int TOK_PLUS = TOK_COMMA + 1;

    // "-"
    static final int TOK_MINUS = TOK_PLUS + 1;

    // "="
    static final int TOK_EQUALS = TOK_MINUS + 1;

    // "!+"
    static final int TOK_NOT_EQUALS = TOK_EQUALS + 1;

    // ">"
    static final int TOK_GT = TOK_NOT_EQUALS + 1;

    // "<"
    static final int TOK_LT = TOK_GT + 1;

    // ">="
    static final int TOK_GTE = TOK_LT + 1;

    // "<="
    static final int TOK_LTE = TOK_GTE + 1;

    // "and"
    static final int TOK_AND = TOK_LTE + 1;

    // "or"  (why is this different from TOK_VBAR?)
    static final int TOK_OR = TOK_AND + 1;

    // "mod"
    static final int TOK_MOD = TOK_OR + 1;

    // "div"
    static final int TOK_DIV = TOK_MOD + 1;

    int currentToken = TOK_EOF;
    String currentTokenValue = null;

    private int currentTokenStartIndex = 0;
    private final String expr;
    private int exprIndex = 0;
    private int exprLength;
    private boolean recognizeOperator = false;
    
    /**
     * construct with the string to be tokenized
     */
    ExprTokenizer(String s) 
    {
        this.expr = s;
        this.exprLength = s.length();
    }

    /**
     * lexes the next token, leaving the token type in
     * <code>currentToken</code>, and the value (if applicable) in
     * <code>tokenValue</code>
     *
     */
    void next() throws ParseException 
    {
        currentTokenValue = null;
        currentTokenStartIndex = exprIndex;

        // a mode switch
        boolean currentMaybeOperator = recognizeOperator;
        recognizeOperator = true;

        for (;;) {
            if (exprIndex >= exprLength) {
                currentToken = TOK_EOF;
                return;
            }
            char c = expr.charAt(exprIndex++);

            switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                currentTokenStartIndex = exprIndex;
                break;

            case '<':
                recognizeOperator = false;
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '=') {
                    exprIndex++;
                    currentToken = TOK_LTE;
                }
                else
                    currentToken = TOK_LT;
                return;

            case '>':
                recognizeOperator = false;
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '=') {
                    exprIndex++;
                    currentToken = TOK_GTE;
                }
                else
                    currentToken = TOK_GT;
                return;

            case '/':
                recognizeOperator = false;
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '/') {
                    exprIndex++;
                    currentToken = TOK_SLASH_SLASH;
                }
                else
                    currentToken = TOK_SLASH;
                return;

            case '=':
                recognizeOperator = false;
                currentToken = TOK_EQUALS;
                return;

            case '!':
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '=') {
                    exprIndex++;
                    currentToken = TOK_NOT_EQUALS;
                    recognizeOperator = false;
                    return;
                }
                throw new ParseException("illegal character");

            case ',':
                recognizeOperator = false;
                currentToken = TOK_COMMA;
                return;

            case '|':
                recognizeOperator = false;
                currentToken = TOK_VBAR;
                return;

            case '+':
                recognizeOperator = false;
                currentToken = TOK_PLUS;
                return;

            case '-':
                recognizeOperator = false;
                currentToken = TOK_MINUS;
                return;

            case '(':
                currentToken = TOK_LPAR;
                recognizeOperator = false;
                return;

            case ')':
                currentToken = TOK_RPAR;
                return;

            case '[':
                currentToken = TOK_LSQB;
                recognizeOperator = false;
                return;

            case ']':
                currentToken = TOK_RSQB;
                return;

            case '"':
            case '\'':
                exprIndex = expr.indexOf(c, exprIndex);
                if (exprIndex < 0) {
                    exprIndex = currentTokenStartIndex + 1;
                    throw new ParseException("missing quote");
                }
                currentTokenValue = expr.substring(currentTokenStartIndex + 1,
                                                   exprIndex++);
                currentToken = TOK_LITERAL;
                return;

            case '$':
                scanName();
                if (exprIndex == currentTokenStartIndex + 1)
                    throw new ParseException("illegal character");
                if (exprIndex < exprLength && expr.charAt(exprIndex) == ':') {
                    exprIndex++;
                    scanName();
                    if (expr.charAt(exprIndex - 1) == ':')
                        throw new ParseException("bad character after :");
                }
                currentTokenValue = expr.substring(currentTokenStartIndex + 1,
                                                   exprIndex);
                currentToken = TOK_VARIABLE_REF;
                return;

            case '*':
                if (currentMaybeOperator) {
                    recognizeOperator = false;
                    currentToken = TOK_MULTIPLY;
                }
                else
                    currentToken = TOK_STAR;
                return;

            case '@':
                currentToken = TOK_AT;
                recognizeOperator = false;	  
                return;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                scanDigits();
                if (exprIndex < exprLength
                    && expr.charAt(exprIndex) == '.') {
                    exprIndex++;
                    if (exprIndex < exprLength
                        && isDigit(expr.charAt(exprIndex))) {
                        exprIndex++;
                        scanDigits();
                    }
                }
                currentTokenValue = expr.substring(currentTokenStartIndex,
                                                   exprIndex);
                currentToken = TOK_NUMBER;
                return;

            case '.':
                if (exprIndex < exprLength && 
                    isDigit(expr.charAt(exprIndex))) {

                    ++exprIndex;
                    scanDigits();
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       exprIndex);
                    currentToken = TOK_NUMBER;
                    return;
                }
                if (exprIndex < exprLength && expr.charAt(exprIndex) == '.') {
                    exprIndex++;
                    currentToken = TOK_DOT_DOT;
                }
                else
                    currentToken = TOK_DOT;
                recognizeOperator = false;	  
                return;

            default:
                --exprIndex;
                scanName();
                if (exprIndex == currentTokenStartIndex) {
                    throw new ParseException("illegal character");
                }
                if (isAxis()) {
                    recognizeOperator = false;
                    currentToken = TOK_AXIS;
                    return;
                }
                if (exprIndex < exprLength && expr.charAt(exprIndex) == ':') {
                    exprIndex++;
                    if (exprIndex < exprLength && 
                        expr.charAt(exprIndex) == '*') {

                        currentTokenValue = 
                            expr.substring(currentTokenStartIndex,
                                           exprIndex++ - 1);
                        currentToken = TOK_NAME_COLON_STAR;
                        return;
                    }

                    scanName();
                    if (expr.charAt(exprIndex - 1) == ':') {
                        throw new ParseException("bad character after :");
                    }
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       exprIndex);
                    if (followingParen()) {
                        recognizeOperator = false;
                        currentToken = TOK_CNAME_LPAR;
                    }
                    else {
                        currentToken = TOK_QNAME;
                    }
                    return;
                }
                currentTokenValue = expr.substring(currentTokenStartIndex,
                                                   exprIndex);
                if (currentMaybeOperator) {
                    if (currentTokenValue.equals("and"))
                        currentToken = TOK_AND;
                    else if (currentTokenValue.equals("or"))
                        currentToken = TOK_OR;
                    else if (currentTokenValue.equals("mod"))
                        currentToken = TOK_MOD;
                    else if (currentTokenValue.equals("div"))
                        currentToken = TOK_DIV;
                    else
                        throw new ParseException("unrecognized operator name");
                    recognizeOperator = false;
                    return;
                }
                if (followingParen()) {
                    if (currentTokenValue.equals("processing-instruction"))
                        currentToken = TOK_PROCESSING_INSTRUCTION_LPAR;
                    else if (currentTokenValue.equals("comment"))
                        currentToken = TOK_COMMENT_LPAR;
                    else if (currentTokenValue.equals("node"))
                        currentToken = TOK_NODE_LPAR;
                    else if (currentTokenValue.equals("text"))
                        currentToken = TOK_TEXT_LPAR;
                    else
                        currentToken = TOK_FUNCTION_LPAR;
                    recognizeOperator = false;
                }
                else
                    currentToken = TOK_QNAME;
                return;
            }
        }
    }

    private void scanName() 
    {
        if (exprIndex < exprLength
            && isNameStartChar(expr.charAt(exprIndex)))
            while (++exprIndex < exprLength
                   && isNameChar(expr.charAt(exprIndex)))
                ;
    }

    private void scanDigits() 
    {
        while (exprIndex < exprLength && isDigit(expr.charAt(exprIndex)))
            exprIndex++;
    }

    private boolean followingParen() 
    {
        for (int i = exprIndex; i < exprLength; i++) {
            switch (expr.charAt(i)) {
            case '(':
                exprIndex = i + 1;
                return true;
            case ' ':
            case '\r':
            case '\n':
            case '\t':
                break;
            default:
                return false;
            }
        }
        return false;
    }

    private boolean isAxis() 
    {
        for (int i = exprIndex; i < exprLength; i++) {
            switch (expr.charAt(i)) {
            case ':':
                if (i + 1 < exprLength && expr.charAt(i + 1) == ':') {
                    currentTokenValue = expr.substring(currentTokenStartIndex,
                                                       exprIndex);
                    exprIndex = i + 2;
                    return true;
                }
                break;
            case ' ':
            case '\r':
            case '\n':
            case '\t':
                break;
            default:
                return false;
            }
        }
        return false;
    }

    static private final String nameStartChars =
        "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static private final String nameChars = ".-0123456789";
    static private final String digits = "0123456789";

    private static final boolean isDigit(char c) 
    {
        return digits.indexOf(c) >= 0;
    }

    private static final boolean isSpace(char c) 
    {
        switch(c) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            return true;
        }
        return false;
    }

    private static final boolean isNameStartChar(char c) 
    {
        return nameStartChars.indexOf(c) >= 0 || c >= 0x80;
    }
    
    private static final boolean isNameChar(char c) 
    {
        return nameStartChars.indexOf(c) >= 0 || 
            nameChars.indexOf(c) >= 0 || c >= 0x80;
    }

}
