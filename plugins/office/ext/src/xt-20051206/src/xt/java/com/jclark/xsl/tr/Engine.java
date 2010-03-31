// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import java.io.IOException;
import java.net.URL;

/**
 * An engine, compiles a stylesheet
 */
public interface Engine
{
    /**
     *
     */
    LoadContext getSheetLoadContext();

    /**
     *
     */
    Sheet createSheet(Node node) throws IOException, XSLException;

    /**
     *
     */
    Sheet createSheet(Node node, 
                      ActionDebugTarget debugger) 
        throws IOException, XSLException;

    /**
     *
     */
    NameTable getNameTable();
}
