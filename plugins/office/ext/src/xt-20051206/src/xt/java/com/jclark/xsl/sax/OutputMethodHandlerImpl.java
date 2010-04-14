// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import java.io.IOException;

/**
 * produces a DocumentHandler suitable for a given output method
 * i.e text, XML, etc  and destination
 */
public class OutputMethodHandlerImpl implements OutputMethodHandler
{
    private XSLProcessor processor;
    private Destination dest;

    /**
     *
     */
    public OutputMethodHandlerImpl(XSLProcessor processor) 
    {
        this.processor = processor;
    }

    /**
     *
     */
    public OutputMethodHandler createOutputMethodHandler(String uri)
    {
        Destination d = dest.resolve(uri);
        if (d == null) {
            return null;
        }
        OutputMethodHandlerImpl om = new OutputMethodHandlerImpl(processor);
        om.setDestination(d);
        return om;
    }

    /**
     *
     */
    public void setDestination(Destination dest) 
    {
        this.dest = dest;
    }

    static private final String JAVA_OUTPUT_METHOD =
        "http://www.jclark.com/xt/java";

    /**
     * obtain a DocumentHandler appropriate for the named
     * output method and our destination
     */
    public DocumentHandler createDocumentHandler(String name,
                                                 AttributeList atts) 
        throws SAXException, IOException
    {
        DocumentHandler handler = null;
        if (name == null)
            ;
        else if (name.startsWith(JAVA_OUTPUT_METHOD)
                 && (name.lastIndexOf(namespaceSeparator)
                     == JAVA_OUTPUT_METHOD.length())) {
            try {
                Class cls = 
                    Class.forName(name.substring(JAVA_OUTPUT_METHOD.length() + 
                                                 1));
                handler = (DocumentHandler)cls.newInstance();
            }
            catch (ClassNotFoundException e) { }
            catch (InstantiationException e) { }
            catch (IllegalAccessException e) { }
            catch (ClassCastException e) { }
        }
        else if (name.equals("http://www.jclark.com/xt"
                             + namespaceSeparator
                             + "nxml")) {
            handler = new NXMLOutputHandler();
        } else if (name.equals("html")) {
            handler = new HTMLOutputHandler();
        } else if (name.equals("text")) {
            handler = new TextOutputHandler();
        }
        if (handler == null) {
            handler = new XMLOutputHandler();
        }
        if (handler instanceof OutputDocumentHandler) {
            handler = ((OutputDocumentHandler)handler).init(dest, atts);
        }
        return handler;
    }
}
