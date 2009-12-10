/*
 * HeaderTokenizer.java
 * Copyright (C) 2002 The Free Software Foundation
 * 
 * This file is part of GNU JavaMail, a library.
 * 
 * GNU JavaMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GNU JavaMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package javax.mail.internet;

/**
 * A lexer for RFC 822 and MIME headers.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.4
 */
public class HeaderTokenizer
{

  /**
   * A token returned by the lexer. These tokens are specified in RFC 822
   * and MIME.
   */
  public static class Token
  {

    /**
     * An ATOM.
     */
    public static final int ATOM = -1;

    /**
     * A quoted-string.
     * The value of this token is the string without the quotes.
     */
    public static final int QUOTEDSTRING = -2;

    /**
     * A comment.
     * The value of this token is the comment string without the comment 
     * start and end symbols.
     */
    public static final int COMMENT = -3;

    /**
     * The end of the input.
     */
    public static final int EOF = -4;

    /*
     * The token type.
     */
    private int type;

    /*
     * The value of the token if it is of type ATOM, QUOTEDSTRING, or
     * COMMENT.
     */
    private String value;

    /**
     * Constructor.
     * @param type the token type
     * @param value the token value
     */
    public Token(int type, String value)
    {
      this.type = type;
      this.value = value;
    }
    
    /**
     * Returns the token type.
     * If the token is a delimiter or a control character,
     * the type is the integer value of that character.
     * Otherwise, its value is one of the following:
     * <ul>
     * <li>ATOM: a sequence of ASCII characters delimited by either 
     * SPACE, CTL, '(', '"' or the specified SPECIALS
     * <li>QUOTEDSTRING: a sequence of ASCII characters within quotes
     * <li>COMMENT: a sequence of ASCII characters within '(' and ')'
     * <li>EOF: the end of the header
     * </ul>
     */
    public int getType()
    {
      return type;
    }

    /**
     * Returns the value of the token.
     */
    public String getValue()
    {
      return value;
    }

  }

  /**
   * RFC 822 specials.
   */
  public static final String RFC822 = "()<>@,;:\\\"\t .[]";

  /**
   * MIME specials.
   */
  public static final String MIME = "()<>@,;:\\\"\t []/?=";
  
  /*
   * The EOF token.
   */
  private static final Token EOF = new Token(Token.EOF, null);

  /*
   * The header string to parse.
   */
  private String header;

  /*
   * The delimiters.
   */
  private String delimiters;

  /*
   * Whather to skip comments.
   */
  private boolean skipComments;

  /*
   * The index of the character identified as current for the token()
   * call.
   */
  private int pos = 0;

  /*
   * The index of the character that will be considered current on a call to
   * next().
   */
  private int next = 0;

  /*
   * The index of the character that will be considered current on a call to
   * peek().
   */
  private int peek = 0;
  
  private int maxPos;

  /**
   * Constructor.
   * @param header the RFC 822 header to be tokenized
   * @param delimiters the delimiter characters to be used to delimit ATOMs
   * @param skipComments whether to skip comments
   */
  public HeaderTokenizer(String header, String delimiters,
                         boolean skipComments)
  {
    this.header = (header == null) ? "" : header;
    this.delimiters = delimiters;
    this.skipComments = skipComments;
    pos = next = peek = 0;
    maxPos = header.length();
  }

  /**
   * Constructor.
   * Comments are ignored.
   * @param header the RFC 822 header to be tokenized
   * @param delimiters the delimiter characters to be used to delimit ATOMs
   */
  public HeaderTokenizer(String header, String delimiters)
  {
    this(header, delimiters, true);
  }

  /**
   * Constructor.
   * The RFC822-defined delimiters are used to delimit ATOMs.
   * Comments are ignored.
   */
  public HeaderTokenizer(String header)
  {
    this(header, RFC822, true);
  }

  /**
   * Returns the next token.
   * @return the next token
   * @exception ParseException if the parse fails
   */
  public Token next()
    throws ParseException
  {
    pos = next;
    Token token = token();
    next = pos;
    peek = next;
    return token;
  }

  /**
   * Peeks at the next token. The token will still be available to be read
   * by <code>next()</code>.
   * Invoking this method multiple times returns successive tokens,
   * until <code>next()</code> is called.
   * @param ParseException if the parse fails
   */
  public Token peek()
    throws ParseException
  {
    pos = peek;
    Token token = token();
    peek = pos;
    return token;
  }

  /**
   * Returns the rest of the header.
   */
  public String getRemainder()
  {
    return header.substring(next);
  }

  /*
   * Returns the next token.
   */
  private Token token()
    throws ParseException
  {
    if (pos >= maxPos)
      {
        return EOF;
      }
    if (skipWhitespace() == Token.EOF)
      {
        return EOF;
      }
    
    boolean needsFilter = false;
    char c;
    
    // comment
    for (c = header.charAt(pos); c == '('; c = header.charAt(pos))
      {
        int start = ++pos;
        int parenCount = 1;
        while (parenCount > 0 && pos < maxPos)
          {
            c = header.charAt(pos);
            if (c == '\\')
              {
                pos++;
                needsFilter = true;
              }
            else if (c == '\r')
              {
                needsFilter = true;
              }
            else if (c == '(')
              {
                parenCount++;
              }
            else if (c == ')')
              {
                parenCount--;
              }
            pos++;
          }
        
        if (parenCount != 0)
          {
            throw new ParseException("Illegal comment");
          }
        
        if (!skipComments)
          {
            String ret = needsFilter ?
              filter(header, start, pos - 1) :
              header.substring(start, pos - 1);
            return new Token(Token.COMMENT, ret);
          }
        
        if (skipWhitespace() == Token.EOF)
          {
            return EOF;
          }
      }
    
    // quotedstring
    if (c == '"')
      {
        int start = ++pos;
        while (pos < maxPos)
          {
            c = header.charAt(pos);
            if (c == '\\')
              {
                pos++;
                needsFilter = true;
              }
            else if (c == '\r')
              {
                needsFilter = true;
              }
            else if (c == '"')
              {
                pos++;
                String ret = needsFilter ?
                  filter(header, start, pos - 1) :
                  header.substring(start, pos - 1);
                return new Token(Token.QUOTEDSTRING, ret);
              }
            pos++;
          }
        throw new ParseException("Illegal quoted string");
      }
    
    // delimiter
    if (c < ' ' || c >= '\177' || delimiters.indexOf(c) >= 0)
      {
        pos++;
        char[] chars = new char[] { c };
        return new Token(c, new String(chars));
      }
    
    // atom
    int start = pos;
    while (pos < maxPos)
      {
        c = header.charAt(pos);
        if (c < ' ' || c >= '\177' || c == '(' || c == ' ' || c == '"' || 
            delimiters.indexOf(c) >= 0)
          {
            break;
          }
        pos++;
      }
    return new Token(Token.ATOM, header.substring(start, pos));
  }
  
  /*
   * Advance pos over any whitespace delimiters.
   */
  private int skipWhitespace()
  {
    while (pos < maxPos)
      {
        char c = header.charAt(pos);
        if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
          {
            return pos;
          }
        pos++;
      }
    return Token.EOF;
  }

  /*
   * Process out CR and backslash (line continuation) bytes.
   */
  private String filter(String s, int start, int end)
  {
    StringBuffer buffer = new StringBuffer();
    boolean backslash = false;
    boolean cr = false;
    for (int i = start; i < end; i++)
      {
        char c = s.charAt(i);
        if (c == '\n' && cr)
          {
            cr = false;
          }
        else
          {
            cr = false;
            if (!backslash)
              {
                if (c == '\\')
                  {
                    backslash = true;
                  }
                else if (c == '\r')
                  {
                    cr = true;
                  }
                else
                  {
                    buffer.append(c);
                  }
              }
            else
              {
                buffer.append(c);
                backslash = false;
              }
          }
      }
    return buffer.toString();
  }

}

