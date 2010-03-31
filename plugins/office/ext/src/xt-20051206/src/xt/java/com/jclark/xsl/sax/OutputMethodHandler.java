// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.SAXException;
import org.xml.sax.DocumentHandler;
import org.xml.sax.AttributeList;
import java.io.IOException;

/**
 * constructs a DocumentHandler appropriate for a 
 * given output method (which typically is determined 
 *  by the stylesheet) and destination (which is determined externally)
 */
public interface OutputMethodHandler
{
    static final char namespaceSeparator = '^';

    /**
     * construct the DocumentHandler for the given outputMethodName
     *  appropriate for the destination with which this object had been
     *  constructed 
     */
    DocumentHandler createDocumentHandler(String outputMethodName,
                                          AttributeList outputMethodAtts)
        throws IOException, SAXException;

    /**
     * construct one of these guys for a given output URI (destination)
     */
    OutputMethodHandler createOutputMethodHandler(String uri);
}
