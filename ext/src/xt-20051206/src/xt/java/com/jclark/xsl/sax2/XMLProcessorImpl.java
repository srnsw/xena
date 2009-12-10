// $Id$

package com.jclark.xsl.sax2;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Parser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.DocumentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXParseException;

import com.jclark.xsl.om.*;

import com.jclark.xsl.tr.Result;
import com.jclark.xsl.tr.LoadContext;

/**
 * Builds an object model from a SAX event stream
 */
public class XMLProcessorImpl implements XMLProcessorEx
{
    
    // we expect to be able to re-use the parser for resolving
    // xsl:include and xsl:import
    private XMLReader _reader = null;

    private ErrorHandler errorHandler;

    public XMLProcessorImpl()
    {}

    /**
     * construct with a SAX2 XML Reader we may wish to re-use for
     * resolving e.g xsl:include
     */
    public XMLProcessorImpl(XMLReader reader) 
    {
        _reader = reader;
    }

    /**
     *
     */
    public void setXMLReader(XMLReader reader)
    {
        _reader = reader;
    }

    
    /**
     * 
     */
    public void setErrorHandler(ErrorHandler errorHandler) 
    {
        this.errorHandler = errorHandler;
    }

    /**
     * parse the xml stream at <code>source</code> building an
     * object model of all its nodes
     *
     * @return the document root
     */
    public Node load(InputSource source,
                     int documentIndex,
                     LoadContext context,
                     NameTable nameTable)
        throws IOException, XSLException 
    {
        
        try {
            //            System.out.println("XMLProcessorImpl::load(" + source.getSystemID + ")");
            if (_reader == null) {
                //                throw new XSLException("cannot build model without an XMLReader");
                _reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                //   System.out.println("XMLProcessorImpl::load(" + source.getSystemID + ") getting new reader ");
            } else {
                // System.out.println("XMLProcessorImpl::load(" + source.getSystemID + ") re-using reader ");
            }

            // build a (xslt)dom with sax
            SAXTwoOMBuilder builder = 
                new SAXTwoOMBuilderImpl(context,
                                        source.getSystemId(),
                                        documentIndex,
                                        nameTable.getEmptyNamespacePrefixMap(),
                                        source);
            
            _reader.setContentHandler(builder);
            _reader.setDTDHandler(builder);
            _reader.parse(source);             // build the DOM
            return builder.getRootNode();
        }
        catch (SAXException e) {
            Exception wrapped = e.getException();
            if (wrapped == null) {
                throw new XSLException(e.getMessage());
            }
            if (wrapped instanceof XSLException) {
                throw (XSLException)e.getException();
            }
            throw new XSLException(wrapped);
        }
        catch (Exception e) {
            throw new XSLException(e);
        }
    }
    
    /**
     * @return the document root
     */
    public Node load(URL url, 
                     int documentIndex,
                     LoadContext context, 
                     NameTable nameTable) 
        throws IOException, XSLException 
    {
        XMLReader reader = _reader;
        if (true || _reader instanceof XMLFilterImpl) {
            // possibly not re-usable, get a real one?

            //            System.out.println("XMLProcessorImpl::load(" + url + ") getting new reader ");
            try {
                _reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                _reader.setFeature("http://xml.org/sax/features/namespaces", true);
            } catch (Exception e) {
                throw new XSLException(e);
            }
        }
        Node n = load(new InputSource(url.toString()),
                      documentIndex,
                      context,
                      nameTable);

        _reader = reader;
        return n;
    }


    public SAXTwoOMBuilder getConfiguredOMBuilder(String systemId, 
                                                  int documentIndex, 
                                                  LoadContext context,
                                                  NameTable nameTable)
    {
        SAXTwoOMBuilder builder = createBuilder(systemId,
                                                documentIndex,
                                                context,
                                                nameTable);

        return builder;

    }



    /**
     * 
     */
    static public SAXTwoOMBuilder createBuilder(String systemId, 
                                                int documentIndex, 
                                                LoadContext context,
                                                NameTable nameTable) 
    {
        return new SAXTwoOMBuilderImpl(context,
                                       systemId,
                                       documentIndex,
                                       nameTable.getEmptyNamespacePrefixMap(),
                                       null);
    }

    /**
     *
     */
    public Result createResult(Node baseNode,
                               int documentIndex,
                               LoadContext loadContext,
                               Node[] rootNode) throws XSLException 
    {
        
        URL baseURL = null;
        if (baseNode != null) {
            baseURL = baseNode.getURL();
        }
        String base;
        if (baseURL == null) {
            base = null;
        } else {
            base = baseURL.toString();
        }

        // FIXME: fix MultiNamespaceResult to accept SAX 2 Builder

        // construct the object that actually constructs the object model
        // which, as part of the construction process, builds the model
        SAXTwoOMBuilder builder =
            createBuilder(base,
                          documentIndex,
                          loadContext,
                          baseNode.getNamespacePrefixMap().getNameTable());
        
        rootNode[0] = builder.getRootNode();
        return new MultiNamespaceResult(builder, errorHandler);
    }

}
