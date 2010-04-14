// $Id$

package com.jclark.xsl.sax2;

import com.jclark.xsl.sax.Destination;

import org.xml.sax.*;
import java.io.*;
import java.util.Properties;

/**
 * An object which serailizes SAX 2 events to raw text
 */
public class TextOutputHandler
    implements OutputContentHandler
{
    private Writer writer;
    private boolean keepOpen;

    public TextOutputHandler() 
    {}

    public TextOutputHandler(Writer writer) 
    {
        this.writer = writer;
    }

    /**
     * @param props output method parameters
     */
    public ContentHandler init(Destination dest, Properties props)
        throws IOException 
    {
        String mediaType = props.getProperty("media-type");
        if (mediaType == null) {
            mediaType = "text/plain";
        }
        writer = dest.getWriter(mediaType, props.getProperty("encoding"));
        keepOpen = dest.keepOpen();
        return this;
    }

    public void startDocument()
    {}

    public void endDocument() throws SAXException 
    {
        try {
            if (writer != null) {
                if (keepOpen) {
                    writer.flush();
                } else {
                    writer.close();
                }
                writer = null;
            }
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }


    public void characters(char cbuf[], int off, int len) throws SAXException 
    {
        try {
            writer.write(cbuf, off, len);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startElement(String namespace, String localName, 
                             String qname, Attributes atts)
    {
        // we only do characters
    }

    public void endElement(String namespace, String localName, 
                           String qname)
    {
        // we only do characters
    }

    public void setDocumentLocator(Locator loc)
    {}

    public void skippedEntity(String name)
    {}

    public void startPrefixMapping(String prefix, String namespace)
    {}

    public void endPrefixMapping(String prefix)
    {}

    public void processingInstruction(String target, String value)
    {}

    public void ignorableWhitespace(char[] buf, int start, int len)
    {}

}
