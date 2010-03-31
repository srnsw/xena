// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import java.net.URL;
import java.io.IOException;

/**
 * Constructs an object model from an XML document
 */
public interface XMLProcessor
{
    /**
     * construct an object model from the XML source at the
     * given URL.
     *
     * @param url the source XML
     * @param documentIndex an internal identifier for this document
     * @param loadContext parameters controlling whether e.g. comment
     *          nodes should be included in the model
     * @param nameTable initial set of in-scope namespace bindings
     */
    Node load(URL url, int documentIndex, 
              LoadContext context, NameTable nameTable) 
        throws IOException, XSLException;

    /**
     *
     */
    Result createResult(Node baseNode,
                        int documentIndex,
                        LoadContext loadContext,
                        Node[] rootNodeRef) 
        throws XSLException;
}
