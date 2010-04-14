// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;
import com.jclark.xsl.tr.LoadContext;
import com.jclark.xsl.tr.Result;
import com.jclark.xsl.sax.XMLProcessorEx;
import com.jclark.xsl.sax.MultiNamespaceResult;
import java.net.URL;
import java.io.IOException;
import org.xml.sax.*;

/**
 * builds a om.Node from a DOM implementation
 */
public abstract class XMLProcessorImpl 
    implements XMLProcessorEx, DOMExtensions
{

    private ErrorHandler errorHandler;

    public Node load(URL url, int documentIndex, 
                     LoadContext context, 
                     NameTable nameTable)
        throws IOException, XSLException
    {
        return load(new InputSource(url.toString()),
                    documentIndex,
                    context,
                    nameTable);
    }

    public  Node load(InputSource source,
                      int documentIndex, 
                      LoadContext context,
                      NameTable nameTable)
        throws IOException, XSLException
    {
        try {
            org.w3c.dom.Document doc = load(source);
            return new RootNode(doc, this, context, 
                                nameTable, source.getSystemId(), 
                                documentIndex);
        }
        catch (SAXParseException e) {
            throw new XSLException(e);
        }
        catch (SAXException e) {
            Exception wrapped = e.getException();
            if (wrapped == null)
                throw new XSLException(e.getMessage());
            if (wrapped instanceof XSLException)
                throw (XSLException)e.getException();
            throw new XSLException(wrapped);
        }
    }

    /**
     *
     */
    public void setErrorHandler(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    /**
     */
    public abstract org.w3c.dom.Document load(InputSource input)
        throws IOException, SAXException;

    /**
     *
     */
    public org.w3c.dom.Element getElementById(org.w3c.dom.Document doc,
                                              String str)
    {
        return null;
    }

    /**
     *
     */
    public Result createResult(Node baseNode,
                               int documentIndex,
                               LoadContext loadContext,
                               Node[] rootNodeRef) throws XSLException
    {
        if (baseNode == null)
            throw new XSLException("cannot convert result tree fragment returned by extension function to a node-set with the DOM");
        RootNode root = ((NodeBase)baseNode).root;
        org.w3c.dom.DocumentFragment docFrag
            = root.ownerDocument.createDocumentFragment();
        String base = null;
        URL baseURL = baseNode.getURL();
        if (baseURL != null)
            base = baseURL.toString();
        rootNodeRef[0] =
            new RootNode(docFrag,
                         this,
                         loadContext,
                         baseNode.getNamespacePrefixMap().getNameTable(),
                         base,
                         documentIndex);
        return new MultiNamespaceResult(new DOMBuilder(docFrag), 
                                        errorHandler);
    }

}
