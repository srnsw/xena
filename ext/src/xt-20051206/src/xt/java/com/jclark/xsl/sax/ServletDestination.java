// $Id$

package com.jclark.xsl.sax;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.servlet.ServletResponse;

/**
 * provides and initializes an output stream to (through)
 * a ServletResponse
 */
public class ServletDestination extends GenericDestination
{
    private final ServletResponse _response;

    public ServletDestination(ServletResponse response)
    {
        _response = response;
    }

    /**
     * gets an OutputStream for the named (Java style) character encoding
     * and mime content type. Sets the ServletResponse's content type 
     * and encoding
     */
    public OutputStream getOutputStream(String contentType, String encoding)
        throws IOException
    {
        setEncoding(encoding);
        String lowerContentType = contentType.toLowerCase().trim();
        if (lowerContentType.startsWith("text") && 
            lowerContentType.indexOf("charset") < 0) {
            contentType = contentType + "; charset=" + getEncoding();
            _response.setContentType(contentType);
            if (false) {
                // Disabled because getCharacterEncoding is broken in JSDK 2.1
                encoding = _response.getCharacterEncoding();
                //                System.err.println("Set content-type to " + contentType + "; encoding was " + encoding);
                if (encoding != null) {
                    setEncoding(encoding);
                }
            }
        } else {
            _response.setContentType(contentType);
        }
        return _response.getOutputStream();
    }
}

