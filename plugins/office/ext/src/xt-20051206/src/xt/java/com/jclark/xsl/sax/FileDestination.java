// $Id$

package com.jclark.xsl.sax;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

/**
 * Represents a file to which we can write the results of
 * a transformation
 */
public class FileDestination extends GenericDestination 
{
    private final File file;

    public FileDestination(String str) 
    {
        this.file = new File(str);
    }

    public FileDestination(File file) 
    {
        this.file = file;
    }

    public OutputStream getOutputStream(String contentType, String encoding)
        throws IOException 
    {
        setEncoding(encoding);
        return new FileOutputStream(file);
    }

    public Destination resolve(String uri) 
    {
        if (File.separatorChar != '/') {
            uri = uri.replace('/', File.separatorChar);
        }
        File f = new File(uri);
        if (!f.isAbsolute()) {
            f = new File(file.getParent(), uri);
        }
        return new FileDestination(f);
    }

}
