// $Id$

package com.jclark.xsl.sax2;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import java.io.IOException;

import java.util.Properties;

/**
 * constructs a ContentHandler appropriate for a 
 * given output method (which typically is determined 
 *  by the stylesheet) and destination (which is determined externally)
 */
public interface OutputMethodHandler
{
    static final char namespaceSeparator = '^';

    /**
     * construct the ContHandler for the given outputMethodName
     *  appropriate for the destination with which this object had been
     *  constructed 
     */
    ContentHandler createContentHandler(String outputMethodName,
                                        Properties outputMethodProps)
        throws IOException, SAXException;

    /**
     * construct one of these guys for a given output URI (destination)
     */
    OutputMethodHandler createOutputMethodHandler(String uri);
}


