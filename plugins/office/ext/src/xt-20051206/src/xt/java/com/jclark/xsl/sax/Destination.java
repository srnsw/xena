// $Id$

package com.jclark.xsl.sax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * represents the output destination for an XML document
 */
public interface Destination 
{
    /**
     *
     */
    OutputStream getOutputStream(String contentType, String encoding)
        throws IOException;

    /**
     *
     */
    Writer getWriter(String contentType, String encoding)
        throws IOException, UnsupportedEncodingException;

    /**
     * Returns true if the OutputStream or Writer should be kept open by the
     * caller and not closed when the caller is done with it.
     */
    boolean keepOpen();

    /**
     * Returns the IANA name of the encoding actually used.
     */
    String getEncoding();

    /**
     *
     */
    Destination resolve(String uri);
}
