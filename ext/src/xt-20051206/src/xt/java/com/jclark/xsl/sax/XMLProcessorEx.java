// $Id$

package com.jclark.xsl.sax;

import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.om.Node;
import com.jclark.xsl.om.NameTable;
import com.jclark.xsl.tr.XMLProcessor;
import com.jclark.xsl.tr.LoadContext;

import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

import java.io.IOException;

/**
 * extended public interface for an XMLProcessor, which loads an XML
 *  source into our own  DOM-like structure
 */
public interface XMLProcessorEx extends XMLProcessor
{
    /**
     * @param source the input to our parser
     * @param documentIndex so we can distinguish between nodes in different docs
     * @param context ??
     * @param nameTable we intern names to avoid wasting menmory
     */
    Node load(InputSource source, int documentIndex, LoadContext context,
              NameTable nameTable)
        throws IOException, XSLException;

    /**
     * @param ErrorHandler a SAX errorHandler where we report parse errors
     */
    void setErrorHandler(ErrorHandler errorHandler);
}
