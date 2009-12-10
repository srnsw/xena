// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;

/**
 * an object which is able to provide a SAX2 XMLFilter
 * We use these for some experimental XSLT extension elements
 */
public interface SaxFilterMaker
{
    /**
     * return an object we can attach in a pipe
     */
    public XMLFilter getFilter();

}
