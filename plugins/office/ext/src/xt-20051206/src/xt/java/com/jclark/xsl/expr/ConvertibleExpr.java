// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * An XPath expression (component) which can be cast to
 *  any of several types as needed
 */
abstract class ConvertibleExpr
{
    /**
     * cast it as a String expression
     */
    abstract ConvertibleStringExpr makeStringExpr();

    /**
     * cast it as a boolean expression
     */
    abstract ConvertibleBooleanExpr makeBooleanExpr();

    /**
     * cast it as a Variant (a variable or param you can 
     * bind to a name) expression
     */
    abstract ConvertibleVariantExpr makeVariantExpr();
  
    /**
     * cast it as a NodeSet expression (by default, don't)
     */
    ConvertibleNodeSetExpr makeNodeSetExpr() 
        throws ParseException
    {
        throw new ParseException("value of expression cannot be converted to a node-set");
    }

    /**
     * cast it as a Number expression
     */
    ConvertibleNumberExpr makeNumberExpr()
    {
        return makeStringExpr().makeNumberExpr();
    }

    /**
     * cast it as a Predicate expression
     */
    ConvertibleBooleanExpr makePredicateExpr()
    {
        return makeBooleanExpr();
    }

}
