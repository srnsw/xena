// $Id$

package com.jclark.xsl.sax;

import java.io.OutputStream;

/**
 * A Destination on an already opened OutputStream
 */
public class OutputStreamDestination extends GenericDestination
{
    final private OutputStream outputStream;

    public OutputStreamDestination(OutputStream outputStream) 
    {
        this.outputStream = outputStream;
    }

    /**
     * initialize with the given (Java) character encoding name,
     * ignore the mime contentType and return the OutputStream
     * for an OutputHandler to write to
     */
    public OutputStream getOutputStream(String contentType, String encoding) 
    {
        setEncoding(encoding);
        return outputStream;
    }
}
