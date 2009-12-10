// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
// import com.jclark.xsl.sax.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.regex.Matcher;


import java.net.URL;

/**
 * Represents the EXSL exsl:object-type function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/exsl/functions/object-type/exsl.object-type.html">Specification</A>. 
 */
class ObjectTypeFunction extends Function1 
{
    /**
     *
     */
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e) throws ParseException 
    {

        ConvertibleExpr objType = new LiteralExpr("string");


        if (e instanceof ConvertibleVariantExpr) {
            final VariantExpr ve = e.makeVariantExpr();
            final Name varName = (e instanceof GlobalVariableRefExpr) ? 
                                  ((GlobalVariableRefExpr)e).getName() : null;

            return new ConvertibleStringExpr() {

                    public String eval(Node node, ExprContext context)
                        throws XSLException 
                    {

                        if (varName != null  && varName.getNamespace() != null) {
                            if (context.getExtensionContext(varName.getNamespace())
                                .available(varName.getLocalPart())) {
                                return "external";
                            }
                        }

                        Variant v = ve.eval(node, context);
                        
                        if (v.isBoolean()) {
                            return "boolean";
                        } else if (v.isNumber()) {
                            return "number";
                        } else if (v.isString()) {
                            return "string";
                        } else if (v.isNodeSet()) {
                            return "node-set";
		        } else if (v instanceof com.jclark.xsl.tr.ResultFragmentVariant) {
                            return "RTF";
                        }
                        return "unknown";
                    }
                };
            
        } else if (e instanceof ConvertibleNumberExpr) {
            objType = new LiteralExpr("number");
        } else if (e instanceof ConvertibleBooleanExpr) {
            objType = new LiteralExpr("boolean");
        } else if (e instanceof ConvertibleNodeSetExpr) {
            objType = new LiteralExpr("boolean");
        } else if (e instanceof ConvertibleStringExpr) {
            objType = new LiteralExpr("string");
        }
            
        return objType;

    }
    
}


