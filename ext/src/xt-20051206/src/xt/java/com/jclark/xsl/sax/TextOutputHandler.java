// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import java.io.*;

/**
 * An object which serailizes SAX 1 events to raw text
 */
public class TextOutputHandler extends HandlerBase 
    implements OutputDocumentHandler
{
    private Writer writer;
    private boolean keepOpen;

    public TextOutputHandler() 
    {
    }

    public TextOutputHandler(Writer writer) 
    {
        this.writer = writer;
    }

    public DocumentHandler init(Destination dest, AttributeList atts)
        throws IOException 
    {
        String mediaType = atts.getValue("media-type");
        if (mediaType == null) {
            mediaType = "text/plain";
        }
        writer = dest.getWriter(mediaType, atts.getValue("encoding"));
        keepOpen = dest.keepOpen();
        return this;
    }

    public void endDocument() throws SAXException 
    {
        try {
            if (writer != null) {
                if (keepOpen)
                    writer.flush();
                else
                    writer.close();
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
}
