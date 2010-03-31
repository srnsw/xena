// $Id$

package com.jclark.xsl.sax2;

import com.jclark.xsl.sax.CommentHandler;

import com.jclark.xsl.om.*;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

/**
 * constructs an (XT) object model from SAX2 events
 */ 
public interface SAXTwoOMBuilder extends ContentHandler, 
                                         CommentHandler, DTDHandler 
{
    /**
     * @return the root node of the constructed model
     */
    public Node getRootNode();
}

