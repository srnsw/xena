// $Id$
package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

import java.net.URLDecoder;

import java.io.UnsupportedEncodingException;

/**
 * Represents the EXSL str:encode-uri function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/str/functions/encode-uri/str.encode-uri.html">Specification</A>. 
 */
class DecodeURIFunction implements Function 
{


    static String dfltEncName = "UTF-8";
    
    /**
     *
     */
    public ConvertibleExpr makeCallExpr(ConvertibleExpr[] args,
                                        Node exprNode) throws ParseException 
    {
        if (args.length < 1 || args.length > 2) {
            throw new ParseException("expected 1 or 2 arguments");
        }
	
        final StringExpr se = args[0].makeStringExpr();
	final StringExpr se2 = (args.length == 1
                                ? new LiteralExpr("") : args[1].makeStringExpr());
	
	return new ConvertibleStringExpr() {
                public String eval(Node node, ExprContext context) throws XSLException {
                    return decodeURI(se.eval(node, context),
				     se2.eval(node, context));
		}
            };
    }
    
    static final private String decodeURI(String src, String enc) 
    {
	try {
	    if ("".equals(enc)){
		enc = dfltEncName;
	    }
	    
	    return URLDecoder.decode(src, enc);
	    
	} catch (UnsupportedEncodingException uex) {
	    //encoding unsupported exception return empty string
	    return "";
	} catch (Exception ex) {
	    return "";
	}
    }
}


    
