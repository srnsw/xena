// $Id$

package com.jclark.xsl.sax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.File;
import java.io.OutputStreamWriter;

/**
 * A base class for output Destinations, providing some common
 * default behaviors
 */
public abstract class GenericDestination implements Destination 
{
    private String _encoding;

    /**
     * returns the IANA character encoding name
     */
    public String getEncoding() 
    {
        return _encoding;
    }

    /**
     * sets the IANA character encoding name from a Java character
     *  encoding name.
     */
    protected void setEncoding(String encoding) 
    {
        if (encoding == null) {
            _encoding = "UTF-8";
        } else {
            _encoding = EncodingName.toIana(encoding);
        }
    }

    /**
     * determines whether the output stream should be closed when
     * the transformation is completed.  The default value is <code>true</code>
     */
    public boolean keepOpen() 
    {
        return false;
    }

    /**
     * get a Writer for an OutputHandler to write characters to
     * our default behavior is to construct a new Writer around
     * the OutputStream associated with this Destination.
     */
    public Writer getWriter(String contentType, String encoding)
        throws IOException, UnsupportedEncodingException 
    {
        OutputStream out = getOutputStream(contentType, encoding);

        if (out == null) {
            throw new IOException("null outputStream");
        }
        return new OutputStreamWriter(out, EncodingName.toJava(getEncoding()));
    }

    /**
     * By default return a FileDestination to the given (local) URI
     */
    public Destination resolve(String uri)
    {
        if (File.separatorChar != '/') {
            uri = uri.replace('/', File.separatorChar);
        }
        return new FileDestination(uri);
    }
}
