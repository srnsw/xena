// $Id$
package com.jclark.xsl.trax;


import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import com.jclark.xsl.sax2.XSLProcessor;
import com.jclark.xsl.sax2.SAXTwoOMBuilder;
import com.jclark.xsl.sax2.XSLProcessorImpl;


/**
 * An implementation of javax.xml.transform.sax.TransformerHandler
 * A TransformerHandler listens for SAX ContentHandler parse
 *  events and transforms them to a Result. 
 */
class TransformerHandlerImpl extends XMLFilterImpl
    implements TransformerHandler 
{
    
    private String _systemId;
    private TransformerImpl _transformer;
    private Result _result;

    private XSLProcessorImpl _processor = null;
    private SAXTwoOMBuilder _omBuilder = null;


    public TransformerHandlerImpl(TransformerImpl transformer) throws SAXException
    {
        _transformer = transformer;
    }
    
    
    public void setSystemId(String systemId)
    {
        _systemId = systemId;
    }
    
    
    public String getSystemId()
    {
        return _systemId;
    }
    
    
    public Transformer getTransformer()
    {
        return _transformer;
    }
    
    
    public void setResult(Result result)
        throws IllegalArgumentException
    {

        if (result == null ) {
            throw new IllegalArgumentException("null result");
        }
        _result = result;
        try {
            _processor = _transformer.init(result);

            _processor.setSourceReader(this);

            _processor.configureResult();

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }


    public void startDocument() throws SAXException
    {
        _omBuilder = _processor.getSourceOMBuilder(_systemId);
        super.setContentHandler(_omBuilder);
        super.setDTDHandler(_omBuilder);

        super.startDocument();

    }


    public void endDocument() throws SAXException
    {
        super.endDocument();
        try {
            _processor.transform(_omBuilder.getRootNode());
        } catch (Exception ex) {
            throw new SAXException(ex);
        }

    }

    
    public void notationDecl(String s1, String s2, String s3)
    {
        
    }
    
    public void unparsedEntityDecl(String s1, String s2, String s3, String s4)
    {
        // FIXME: implement
    }
    
    public void comment(char[] buffer, int start, int end)
    {
        // FIXME: implement
    }
    
    public void startCDATA()
    {
    }
    
    public void endCDATA()
    {
    }
    
    public void startDTD(String s1, String s2, String s3)
    {
    }
    
    public void endDTD()
    {
    }
    
    public void startEntity(String s)
    {
    }
    
    public void endEntity(String s)
    {
    }
    
}

