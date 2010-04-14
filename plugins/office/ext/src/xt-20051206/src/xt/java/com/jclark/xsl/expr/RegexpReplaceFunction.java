// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.regex.Matcher;


/**
 * Represents the EXSL regexp:replace function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/regexp/functions/replace/regexp.replace.html">Specification</A>. 
 */
class RegexpReplaceFunction implements Function 
{
    /**
     *
     */
    public ConvertibleExpr makeCallExpr(ConvertibleExpr[] args,
                                        Node exprNode) throws ParseException 
    {
        if (args.length != 4) {
            throw new ParseException("expected 4 arguments");
        }
        final StringExpr se = args[0].makeStringExpr();
        final StringExpr se2 = args[1].makeStringExpr();
	final StringExpr se3 = args[2].makeStringExpr();
	final StringExpr se4 = args[3].makeStringExpr();
        // final StringExpr se3 = (args.length == 2
	//                                 ? new LiteralExpr("") : args[2].makeStringExpr());
	
        return new ConvertibleStringExpr() {
                public String eval(Node node, 
				   ExprContext context) 
                    throws XSLException 
                {
                    return replace(node, 
				   context,
				   se.eval(node, context),
				   se2.eval(node, context),
				   se3.eval(node, context),
				   se4.eval(node, context));
                }
            };
    }
    

    /**
     *
     */
    static final private String replace(Node node, 
					ExprContext context,
					String src, String pattern, String flags, 
					String replaceStr) 
	throws XSLException
    {
        // FIXME: make use of the flags argument
	try {
	    
	    boolean globalReplace = false;
	    boolean ignoreCase = false;
	    
	    if ( flags.length() > 0 ) {
		globalReplace = flags.indexOf("g") < 0 ? false : true;
		ignoreCase = flags.indexOf("i") < 0 ? false : true;
	    }

	    Pattern pat  
		= ignoreCase ? Pattern.compile(pattern,
					       Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
	    Matcher matcher = pat.matcher(src);
            if (matcher.find()) {
                int gc = matcher.groupCount();
		//System.out.println("can replace" + gc + " groups");
		return globalReplace ? matcher.replaceAll(replaceStr) :
		    matcher.replaceFirst(replaceStr);
	    } else {
                return src;
            }
        } catch (PatternSyntaxException ex) { 
            return null;
	} catch (Exception e) {
	    return null;
	}
    }
}

