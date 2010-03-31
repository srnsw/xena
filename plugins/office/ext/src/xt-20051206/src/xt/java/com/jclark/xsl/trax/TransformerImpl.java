// $Id$

package com.jclark.xsl.trax;

import com.jclark.xsl.tr.Sheet;
import com.jclark.xsl.tr.Engine;
import com.jclark.xsl.tr.OutputMethod;

import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.sax2.XSLProcessor;
import com.jclark.xsl.sax2.XSLProcessorImpl;

import com.jclark.xsl.sax2.OutputMethodHandlerImpl;

import com.jclark.xsl.sax.Destination;
import com.jclark.xsl.sax.OutputStreamDestination;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import java.io.OutputStream;

/**
 * An implementation of the TrAX Transformer class.
 * Can transform a source tree into a result tree
 */
class TransformerImpl extends Transformer
{

    private URIResolver _uriResolver;
    
    private ErrorListener _errorListener;
    private XSLProcessorImpl _processor;
    private Sheet _sheet;
    private TransformerFactoryImpl _factory;

    /**
     * Create a TransformerImpl.
     */
    public TransformerImpl(Sheet sheet, Engine engine, 
                           TransformerFactoryImpl factory)
    {
        _sheet = sheet;
        _processor = new XSLProcessorImpl(sheet, engine);
        _factory = factory;
    }
    
    protected XSLProcessorImpl init(Result result)
        throws TransformerException
    {
        
        XSLProcessorImpl processor = (XSLProcessorImpl) _processor.clone();
        
        if (result instanceof StreamResult) {
            StreamResult sr = (StreamResult) result;
            
            OutputMethodHandlerImpl outputMethodHandler = 
                new OutputMethodHandlerImpl();
            
            processor.setOutputMethodHandler(outputMethodHandler);
            Destination dest;
            OutputStream ostream = sr.getOutputStream();
            if (ostream != null) {

                dest = new OutputStreamDestination(ostream);
            } else {
                // FIXME: we need to handle a characterWriter
                throw new TransformerException("cannot use Writer result");
            }
            
            outputMethodHandler.setDestination(dest);
            
        } else if (result instanceof SAXResult) {

            SAXResult sr = (SAXResult) result;
            processor.setContentHandler(sr.getHandler());
            // FIXME: set lexical handler?
        } else {
            throw new TransformerException("unrecognized Result class: " +
                                           result.getClass().getName());
        }
        
        return processor;
    }
    
    /**
     * Process the source tree to the output result.
     */
    public void transform(Source source, Result result)
        throws TransformerException
    {
        
        XSLProcessorImpl processor = init(result);
        
        try {
            // now find an adapter for the input source
            XMLReader reader = _factory.getReader(source);
            processor.setSourceReader(reader);
            String sysId = source.getSystemId();
            InputSource src = SAXSource.sourceToInputSource(source);
            if (src == null) {
                if (sysId == null) {
                    src = new InputSource("dummy");
                } else {
                    src = new InputSource(sysId);
                }
            }
            
            // FIXME: set error handler
            processor.parse(src);
            
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
    
    /**
     * Clear all parameters set with setParameter.
     */
    public void clearParameters()
    {
        _processor.clearParameters();
    }
    
    /**
     * Add a parameter for the transformation.
     */
    public void setParameter(String name, Object value)
    {
        _processor.setParameter(name, value);
    }
    
    /**
     * Get a parameter that was explicitly set with setParameter
     * or setParameters.
     */
    public Object getParameter(String name)
    {
        return _processor.getParameter(name);
    }
    
    /**
     * Get an object that will be used to resolve URIs used in
     * document(), etc.
     */
    public URIResolver getURIResolver()
    {
        return _uriResolver;
    }
    
    /**
     * Set an object that will be used to resolve URIs used in
     * document().
     */
    public void setURIResolver(URIResolver resolver)
    {
        _uriResolver = resolver;
    }
    
    /**
     * Set the output properties for the transformation.  These
     * properties will override properties set in the Templates
     * with xsl:output.
     */
    public void setOutputProperties(Properties oformat)
        throws IllegalArgumentException
    {
        if (oformat == null) {
            _sheet.clearOutputMethodProperties();
        } else {
            for (Enumeration keys = oformat.keys(); keys.hasMoreElements(); ) {
                String key = (String)keys.nextElement();
                String val = oformat.getProperty(key);
                setOutputProperty(key, val);
            }
        }
    }
    
    /**
     * 
     */
    public Properties getOutputProperties()
    {
        return _sheet.getOutputMethodProperties();
    }
    
    /**
     * Set an output property that will be in effect for the
     * transformation.
     */
    public void setOutputProperty(String name, String value)
        throws IllegalArgumentException
    {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        try {
            _sheet.setOutputMethodProperty("", name, value);
        } catch (XSLException ex) {
            throw new IllegalArgumentException(name + " = " + value);
        }
    }
    
    /**
     * Return null. See comment for getOutputProperties().
     */
    public String getOutputProperty(String name)
        throws IllegalArgumentException
    {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        } 
        return _sheet.getOutputMethodProperty("", name);
    }
    
    
    /**
     * Set the error event listener in effect for the transformation.
     */
    public void setErrorListener (ErrorListener listener)
        throws IllegalArgumentException
    {
        _errorListener = listener;
    }
    
    
    /**
     * Get the error event handler in effect for the transformation.
     * @return The current error handler, which should never be null.
     */
    public ErrorListener getErrorListener()
    {
        return _errorListener;
    }
    
}

