// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
public interface Variant 
{
    /**
     *
     */
    String convertToString() throws XSLException;

    /**
     *
     */
    boolean convertToBoolean() throws XSLException;

    /**
     *
     */
    Variant makePermanent() throws XSLException;

    /**
     *
     */
    NodeIterator convertToNodeSet() throws XSLException;

    /**
     *
     */
    double convertToNumber() throws XSLException;

    /**
     *
     */
    Object convertToObject() throws XSLException;

    /**
     *
     */
    boolean convertToPredicate(ExprContext context) throws XSLException;

    /**
     *
     */
    boolean isBoolean();

    /**
     *
     */
    boolean isNumber();

    /**
     *
     */
    boolean isString();

    /**
     *
     */
    boolean isNodeSet();

    /**
     *
     */
    Node getBaseNode();		// for base URI; null if none

}
