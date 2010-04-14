// $Id$

package com.jclark.xsl.sax2;
import com.jclark.xsl.sax.Destination;

import org.xml.sax.*;

import java.util.Properties;
import java.io.IOException;

/**
 * produces a ContentHandler suitable for a given output method
 * i.e text, XML, etc  and destination
 */
public class OutputMethodHandlerImpl implements OutputMethodHandler
{
    private Destination dest;

    /**
     *
     */
    public OutputMethodHandlerImpl() 
    { }

    /**
     * Creates a new instance for writing to the given URI.
     * Useful for "xt:document" extension element
     */
    public OutputMethodHandler createOutputMethodHandler(String uri)
    {
        Destination d = dest.resolve(uri);
        if (d == null) {
            return null;
        }
        OutputMethodHandlerImpl om = new OutputMethodHandlerImpl();
        om.setDestination(d);
        return om;
    }

    /**
     * set the target of our output stream
     */
    public void setDestination(Destination dest) 
    {
        this.dest = dest;
    }

    static private final String JAVA_OUTPUT_METHOD =
        "http://www.jclark.com/xt/java";

    /**
     * obtain a ContentHandler appropriate for the named
     * output method and our destination
     */
    public ContentHandler createContentHandler(String name,
                                               Properties atts) 
        throws SAXException, IOException
    {
        ContentHandler handler = null;
        if (name == null)
            ;

        // FIXME : we probably shouldn't use the same namespace as
        //   we use for SAX 1 handlers
        else if (name.startsWith(JAVA_OUTPUT_METHOD)
                 && (name.lastIndexOf(namespaceSeparator)
                     == JAVA_OUTPUT_METHOD.length())) {
            try {
                Class cls = 
                    Class.forName(name.substring(JAVA_OUTPUT_METHOD.length() + 
                                                 1));
                handler = (ContentHandler)cls.newInstance();
            }
            catch (ClassNotFoundException e) { }
            catch (InstantiationException e) { }
            catch (IllegalAccessException e) { }
            catch (ClassCastException e) { }
        }
        else if (name.equals("http://www.jclark.com/xt"
                             + namespaceSeparator
                             + "nxml")) {

            // FIXME: for these guys, here' where we might handle
            // the possibility of having a character Writer for
            // output, instead of the preferred OutputStream
            handler = new NXMLOutputHandler();
        } else if (name.equals("html")) {
            handler = new HTMLOutputHandler();
        } else if (name.equals("text")) {
            handler = new TextOutputHandler();
        }
        // FIXME: plug in XHTML 

        if (handler == null) {
            handler = new XMLOutputHandler();
        }
        if (handler instanceof OutputContentHandler) {
            handler = ((OutputContentHandler)handler).init(dest, atts);
        }
        return handler;
    }
}

