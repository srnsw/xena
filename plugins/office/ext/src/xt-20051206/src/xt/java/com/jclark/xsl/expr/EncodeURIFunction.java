// $Id$
package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;


import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * Represents the EXSL str:encode-uri function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/str/functions/encode-uri/str.encode-uri.html">Specification</A>. 
 */
class EncodeURIFunction implements Function 
{
    /**
     *
     */
    public ConvertibleExpr makeCallExpr(ConvertibleExpr[] args,
                                        Node exprNode) throws ParseException 
    {
        if (args.length < 2 || args.length > 3) {
            throw new ParseException("expected 2 or 3 arguments");
        }
        final StringExpr se = args[0].makeStringExpr();
        final BooleanExpr se2 = args[1].makeBooleanExpr();
        final StringExpr se3 = (args.length == 2
                                ? new LiteralExpr("") : args[2].makeStringExpr());
	
        return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return encodeURI(se.eval(node, context),
				     se2.eval(node, context),
				     se3.eval(node, context));
                }
            };
    }
    
    

	
    static BitSet dontNeedEncodingRFC2396;
    
    static BitSet dontNeedEncodingRFC2732;

    static final int caseDiff = ('a' - 'A');
    
    static String dfltEncName = "UTF-8";

    static {
	
	/* From URLEncoder java 1.4:
	 *
	 * The list of characters that are not encoded has been
	 * determined as follows:
	 *
	 * RFC 2396 states:
	 * -----
	 * Data characters that are allowed in a URI but do not have a
	 * reserved purpose are called unreserved.  These include upper
	 * and lower case letters, decimal digits, and a limited set of
	 * punctuation marks and symbols. 
	 *
	 * unreserved  = alphanum | mark
	 *
	 * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
	 *
	 * Unreserved characters can be escaped without changing the
	 * semantics of the URI, but this should not be done unless the
	 * URI is being used in a context that does not allow the
	 * unescaped character to appear.
	 * -----
	 *
	 * It appears that both Netscape and Internet Explorer escape
	 * all special characters from this list with the exception
	 * of "-", "_", ".", "*". While it is not clear why they are
	 * escaping the other characters, perhaps it is safest to
	 * assume that there might be contexts in which the others
	 * are unsafe if not escaped. Therefore, we will use the same
	 * list. It is also noteworthy that this is consistent with
	 * O'Reilly's "HTML: The Definitive Guide" (page 164).
	 *
	 * As a last note, Intenet Explorer does not encode the "@"
	 * character which is clearly not unreserved according to the
	 * RFC. We are being consistent with the RFC in this matter,
	 * as is Netscape.
	 *
	 */

	/*
	  
	RFC 2396
	a-z, A-Z, 0-9
	marks:
	"-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
	
	*/
    
	dontNeedEncodingRFC2396 = new BitSet(256);
	int i;
	
	for (i = 'a'; i <= 'z'; i++) {
	    dontNeedEncodingRFC2396.set(i);
	}
	
	for (i = 'A'; i <= 'Z'; i++) {
	    dontNeedEncodingRFC2396.set(i);
	}
	
	for (i = '0'; i <= '9'; i++) {
	    dontNeedEncodingRFC2396.set(i);
	}
	
	dontNeedEncodingRFC2396.set(' '); /* encoding a space to a + is done
					   * in the encode() method */
	dontNeedEncodingRFC2396.set('-');
	dontNeedEncodingRFC2396.set('_');
	dontNeedEncodingRFC2396.set('.');
	dontNeedEncodingRFC2396.set('*');
	
	//FIXME: to add
	//      dontNeedEncodingRFC2396.set('!');
	// 	dontNeedEncodingRFC2396.set('~');
	// 	dontNeedEncodingRFC2396.set("'");
	// 	dontNeedEncodingRFC2396.set('(');
	// 	dontNeedEncodingRFC2396.set(')');
	
	/*
	  RFC  2732
	  reserved characters:
	  ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | "," | "[" | "]".
	*/
	dontNeedEncodingRFC2732 = new BitSet(256);
	dontNeedEncodingRFC2732.set(';');
	dontNeedEncodingRFC2732.set('/');
	dontNeedEncodingRFC2732.set('?');
	dontNeedEncodingRFC2732.set(':');
	dontNeedEncodingRFC2732.set('@');
	dontNeedEncodingRFC2732.set('&');
	dontNeedEncodingRFC2732.set('=');
	dontNeedEncodingRFC2732.set('+');
	dontNeedEncodingRFC2732.set('$');
	dontNeedEncodingRFC2732.set(',');
	dontNeedEncodingRFC2732.set('[');
	dontNeedEncodingRFC2732.set(']');
	
    	// dfltEncName = (String)AccessController.doPrivileged (
	// 	    new GetPropertyAction("file.encoding")
	//     	);
	

    }

    /**
     * You can't call the constructor.
     */
    //private URLEncoder() { }

    // /**
    //      * Translates a string into <code>x-www-form-urlencoded</code>
    //      * format. This method uses the platform's default encoding
    //      * as the encoding scheme to obtain the bytes for unsafe characters.
    //      *
    //      * @param   s   <code>String</code> to be translated.
    //      * @deprecated The resulting string may vary depending on the platform's
    //      *             default encoding. Instead, use the encode(String,String)
    //      *             method to specify the encoding.
    //      * @return  the translated <code>String</code>.
    //      */
    //     public static String encode(String s) {

    // 	String str = null;
	
    // 	try {
    // 	    str = encode(s, dfltEncName);
    // 	} catch (UnsupportedEncodingException e) {
    // 	    // The system should always have the platform default
    // 	}

    // 	return str;
    //     }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code>
     * format using a specific encoding scheme. This method uses the
     * supplied encoding scheme to obtain the bytes for unsafe
     * characters.
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that
     * UTF-8 should be used. Not doing so may introduce
     * incompatibilites.</em>
     *
     * @param   s   <code>String</code> to be translated.
     * @param   enc   The name of a supported 
     *    <a href="../lang/package-summary.html#charenc">character encoding</a>.
     * @return  the translated <code>String</code>.
     */
    static final private String encodeURI(String s, boolean encodeURISubset, String enc) 
    {
	//FIXME: replace unescapable characters %3F
	
	try {
	
	    //set default encoding to UTF-8
	    if ("".equals(enc)){
		enc = dfltEncName;
	    }
	    
	    boolean needToChange = false;
	    boolean wroteUnencodedChar = false; 
	    int maxBytesPerChar = 10; // rather arbitrary limit, but safe for now
	    StringBuffer out = new StringBuffer(s.length());
	    ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
	    
	    OutputStreamWriter writer = new OutputStreamWriter(buf, enc);
	    
	    for (int i = 0; i < s.length(); i++) {
		int c = (int) s.charAt(i);
		//System.out.println("Examining character: " + c);
		//if (dontNeedEncodingRFC2396.get(c)) {
		if (dontNeedEncoding(c, encodeURISubset)) {
		    if (c == ' ') {
			c = '+';
			needToChange = true;
		    }
		    //System.out.println("Storing: " + c);
		    out.append((char)c);
		    wroteUnencodedChar = true;
		} else {
		    // convert to external encoding before hex conversion
		    try {
			if (wroteUnencodedChar) { // Fix for 4407610
			    writer = new OutputStreamWriter(buf, enc);
			    wroteUnencodedChar = false;
			}
			writer.write(c);
			/*
			 * If this character represents the start of a Unicode
			 * surrogate pair, then pass in two characters. It's not
			 * clear what should be done if a bytes reserved in the 
			 * surrogate pairs range occurs outside of a legal
			 * surrogate pair. For now, just treat it as if it were 
			 * any other character.
			 */
			if (c >= 0xD800 && c <= 0xDBFF) {
			    /*
			      System.out.println(Integer.toHexString(c) 
			      + " is high surrogate");
			    */
			    if ( (i+1) < s.length()) {
				int d = (int) s.charAt(i+1);
				/*
				  System.out.println("\tExamining " 
				  + Integer.toHexString(d));
				*/
				if (d >= 0xDC00 && d <= 0xDFFF) {
				    /*
				      System.out.println("\t" 
				      + Integer.toHexString(d) 
				      + " is low surrogate");
				    */
				    writer.write(d);
				    i++;
				}
			    }
			}
			writer.flush();
		    } catch(IOException e) {
			buf.reset();
			continue;
		    }
		    byte[] ba = buf.toByteArray();
		    for (int j = 0; j < ba.length; j++) {
			out.append('%');
			char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
			// converting to use uppercase letter as part of
			// the hex value if ch is a letter.
			if (Character.isLetter(ch)) {
			    ch -= caseDiff;
			}
			out.append(ch);
			ch = Character.forDigit(ba[j] & 0xF, 16);
			if (Character.isLetter(ch)) {
			    ch -= caseDiff;
			}
			out.append(ch);
		    }
		    buf.reset();
		    needToChange = true;
		}
	    }

	    return (needToChange? out.toString() : s);
	    
	} catch (UnsupportedEncodingException uex) {
	    //encoding unsupported exception return empty string
	    return "";
	} catch (Exception ex) {
	    return "";
	}
	
    }
    
    static final private boolean dontNeedEncoding(int c, boolean encodeURISubset) 
    {
	return (encodeURISubset) ? dontNeedEncodingRFC2396.get(c)
	    : dontNeedEncodingRFC2396.get(c) || dontNeedEncodingRFC2732.get(c);
    }
}


    
