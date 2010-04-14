// $Id$

package com.jclark.xsl.sax2;
import com.jclark.xsl.sax.Destination;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Properties;
import java.io.IOException;

/**
 * a SAX2 ContentHandler that serializes to a Destination
 * and requires some initialization
 */
public interface OutputContentHandler extends ContentHandler
{
    /**
     * Initialize the handler with the targetDestination and
     * output method Properties (from xsl:output + calling environment)
     */
    ContentHandler init(Destination dest, Properties outputMethodProperties) 
        throws SAXException, IOException;
}

