// $Id$
package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.regex.Matcher;


/**
 * Represents the EXSL regesp:test function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/regexp/functions/test/regexp.test.html">Specification</A>. 
 */
class RegexpTestFunction implements Function 
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
        final StringExpr se2 = args[1].makeStringExpr();
        final StringExpr se3 = (args.length == 2
                                ? new LiteralExpr("") : args[2].makeStringExpr());

        return new ConvertibleBooleanExpr() {
                public boolean eval(Node node, ExprContext context) throws XSLException {
                    return test(se.eval(node, context),
                                se2.eval(node, context),
                                se3.eval(node, context));
                }
            };
    }


    /**
     *
     */
    static final private boolean test(String src, String pattern, String flags) 
    {
        // FIXME: make use of the flags argument
	//boolean globalReplace = false;
	boolean ignoreCase = false;
	if ( flags.length() > 0 ) {
	    //not used
	    //globalReplace = flags.indexof("g") < 0 ? false : true;
	    ignoreCase = flags.indexOf("i") < 0 ? false : true;
	}
	
        try {
	    //System.out.println("regex : pattern ( " + pattern + "} src {" + src + "}");
	    Pattern pat = ignoreCase ? Pattern.compile(pattern,
						       Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
	    return pat.matcher(src).matches();
	    //return Pattern.matches(pattern, src);
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

}

