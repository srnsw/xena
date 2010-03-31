// $Id$

package com.jclark.xsl.sax2;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A Result Tree Fragment can write out a representation
 *  of itself as a sequence of SAX events
 */
public interface ResultTreeFragment
{
    /**
     * emit a representation of this result tree fragment
     * as a sequence of SAX events to the given ContentHandler
     */
    void emit(ContentHandler handler) 
        throws SAXException;
}

