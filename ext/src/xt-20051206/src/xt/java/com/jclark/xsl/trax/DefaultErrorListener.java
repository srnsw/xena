// $Id$

package com.jclark.xsl.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * A default implementation of the ErrorListener interface.
 * Writes errors and warnings to System.err.
 */
class DefaultErrorListener implements ErrorListener
{
    public static final ErrorListener _instance = new DefaultErrorListener();

    private DefaultErrorListener()
    {}

    /**
     * report an error to stderr
     */
    public void error(TransformerException e)
    {
        System.err.println("Error:");
        e.printStackTrace();
    }

    /**
     * report a fatal error to stderr
     */
    public void fatalError(TransformerException e)
    {
        System.err.println("Fatal Error:");
        e.printStackTrace();
    }

    /**
     * send a warning to stderr
     */
    public void warning(TransformerException e)
    {
        System.err.println("Warning:");
        e.printStackTrace();
    }
}

