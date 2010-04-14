// $Id$

package com.jclark.xsl.sax;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.File;

/**
 * e.g stdout
 */
public class FileDescriptorDestination extends GenericDestination 
{
    private final FileDescriptor fd;

    public FileDescriptorDestination(FileDescriptor fd) 
    {
        this.fd = fd;
    }

    public OutputStream getOutputStream(String contentType, String encoding)
    {
        setEncoding(encoding);
        return new FileOutputStream(fd);
    }

    /**
     * always returns true, we never want to close stdout
     */
    public boolean keepOpen()
    {
        return true;
    }

    /**
     * returns a FileDestination to the given (local) URI
     */
    public Destination resolve(String uri)
    {
        if (File.separatorChar != '/') {
            uri = uri.replace('/', File.separatorChar);
        }
        return new FileDestination(uri);
    }
}
