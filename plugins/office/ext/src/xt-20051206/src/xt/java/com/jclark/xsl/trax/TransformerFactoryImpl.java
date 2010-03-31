// $Id$

package com.jclark.xsl.trax;

import java.io.StringReader;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;

import java.util.Enumeration;
import java.util.Hashtable;

import com.jclark.xsl.sax2.XMLProcessorImpl;


import org.xml.sax.InputSource;

import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.Parser;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;


import javax.xml.transform.*;

import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import javax.xml.parsers.SAXParserFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;


import org.xml.sax.ContentHandler;



/**
 * An implementation of the TrAX TransformerFactory class and
 * SAXTransformerFactory classes.
 * <code><pre>
 *   import java.io.*;
 *   import javax.xml.transform.*;
 *   ...
 *   System.setProperty(&quot;javax.xml.transform.TransformerFactory&quot;,
 *                      &quot;jd.xml.xslt.trax.TransformerFactoryImpl&quot;);
 *   TransformerFactory tfactory = TransformerFactory.newInstance();
 *
 *   Source stylesheetSource = ...
 *   Transformer transformer = tfactory.newTransformer(stylesheetSource);
 *   transformer.transform(...);
 * </pre></code>
 */
public class TransformerFactoryImpl extends SAXTransformerFactory
{


    private ErrorListener _errorListener;

    private URIResolver _uriResolver;

    private Class _readerClass = null; // our default XMLReader class

    /**
     * Process the Source into a Transformer object.
     */
    public Transformer newTransformer(Source source)
        throws TransformerConfigurationException
    {
        return newTemplates(source).newTransformer();
    }
    
    /**
     * Create a new Transformer object that performs a copy
     * of the source to the result.
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     * @exception TransformerConfigurationException May throw this during
     *            the parse when it is constructing the
     *            Templates object and fails.
     */
    public Transformer newTransformer() throws TransformerConfigurationException
    {
        throw new TransformerConfigurationException("not yet implemented");
    }

    /**
     * Process the Source into a Templates object, which is a
     * a compiled representation of the source.
     */
    public Templates newTemplates(Source source)
        throws TransformerConfigurationException
    {

        try {
            XMLReader reader = getReader(source);
            TemplatesHandler th =
                new TemplatesHandlerImpl(this, new XMLProcessorImpl(reader));
            
            String sysId = source.getSystemId();
            
            th.setSystemId(sysId);
            reader.setContentHandler(th);
            
            InputSource src = SAXSource.sourceToInputSource(source);
            if (src == null) {
                if (sysId == null) {
                    src = new InputSource("dummy");
                } else {
                    src = new InputSource(sysId);
                }
            }
            reader.parse(src);
            return th.getTemplates();
        } catch(Exception e) {
            throw toConfigException(e);
        }
    }

    // gets an XMLReader appropiate for the given source
    public XMLReader getReader(Source source) throws Exception
    {

        XMLReader reader;

        if (source instanceof StreamSource) {
            reader = newDefaultReader();
        } else if (source instanceof SAXSource) {
            if ((reader = ((SAXSource)source).getXMLReader()) == null) {
                // a SAXSource without a reader ??
                reader = newDefaultReader();
            }
            //        } else if (source instanceof DOMSource) {
            
        } else {
            throw new Exception("unrecognized input Source type: " +
                                source.getClass().getName());
        }

        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        //        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        return reader;
    }

    //
    private XMLReader newDefaultReader() throws Exception
    {

        XMLReader reader = null;

        try {
            // is this the best / most efficient way?
            reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

        } catch (Exception ex) {


            // hmm can't get a reader with JAXP ??
        }
        if (reader == null) {

            if (_readerClass == null) {
                String parserClassName =
                    System.getProperty("com.jclark.xsl.trax.reader");
                if (parserClassName == null) {
                    parserClassName = System.getProperty("com.jclark.xsl.sax.parser");
                }
                if (parserClassName == null) {
                    parserClassName = System.getProperty("org.xml.sax.parser");
                }
                if (parserClassName == null) {
                    parserClassName = "com.jclark.xml.sax.CommentDriver";
                }
                _readerClass = Class.forName(parserClassName);
            }
            
            Object parserObj = _readerClass.newInstance();
            
            if (parserObj instanceof XMLReader) {
                reader = (XMLReader) parserObj;
            } else {
                reader = new ParserAdapter((Parser) parserObj);
            }
        }
        return reader;

    }

    /**
     * Get the stylesheet specification(s) associated
     * via the xml-stylesheet processing instruction (see
     * http://www.w3.org/TR/xml-stylesheet/) with the document
     * document specified in the source parameter, and that match
     * the given criteria.  Note that it is possible to return several
     * stylesheets, in which case they are applied as if they were
     * a list of imports or cascades.
     * @param source The XML source document.
     * @param media The media attribute to be matched.  May be null, in which
     *              case the prefered templates will be used (i.e. alternate = no).
     * @param title The value of the title attribute to match.  May be null.
     * @param charset The value of the charset attribute to match.  May be null.
     *
     * @return A Source object suitable for passing to the TransformerFactory.
     * @throws TransformerConfigurationException.
     */
    public  Source getAssociatedStylesheet(Source source,
                                           java.lang.String media,
                                           java.lang.String title,
                                           java.lang.String charset)
        throws TransformerConfigurationException
    {
        // FIXME: implement
        throw new TransformerConfigurationException("getAssociatedStylesheet() not yet implemented");
    }

    /**
     * Set the URIResolver.
     */
    public void setURIResolver(URIResolver uriResolver)
    {
        _uriResolver = uriResolver;
    }

    /**
     * Return the URIResolver that was set with setURIResolver.
     * the object that is used by default during the
     *              transformation to resolve URIs used in document(),
     *             xsl:import, or xsl:include.
     */
    public URIResolver getURIResolver()
    {
        return _uriResolver;
    }
    

    /**
     * Look up the value of a feature.
     * <p>The feature name is any absolute URI.</p>
     * @param name The feature name, which is an absolute URI.
     * @return The current state of the feature (true or false).
     */
    public boolean getFeature(String name)
    {
        if (StreamSource.FEATURE.equals(name)) {
            return true;
        }
        if (StreamResult.FEATURE.equals(name)) {
            return true;
        }
        if (DOMSource.FEATURE.equals(name)) {
            return false; // FIXME: implement
        }
        if (DOMResult.FEATURE.equals(name)) {
            return false;  // FIXME: implement
        }
        if (SAXSource.FEATURE.equals(name)) {
            return true;
        }
        if (SAXResult.FEATURE.equals(name)) {
            return true;
        }
        if (SAXTransformerFactory.FEATURE.equals(name)) {
            return true;
        }
        return false;
    }

    /**
     * Throws an IllegalArgumentException since attributes are not supported.
     */
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException("unrecognized attribute " + name);
    }

    /**
     * Throws an IllegalArgumentException since attributes are not supported.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException("unrecognized attribute " + name);
    }

    /**
     * Set the error event listener for the TransformerFactory, which
     * is used for the processing of transformation instructions,
     */
    public void setErrorListener (ErrorListener listener)
        throws IllegalArgumentException
    {
        _errorListener = listener;
    }
    
    /**
     * Get the error event handler for the TransformerFactory.
     * @return The current error handler, which should never be null.
     */
    public ErrorListener getErrorListener()
    {
        return _errorListener;
    }

    /**
     * Get a TemplatesHandler object that can process SAX ContentHandler events into a
     * Templates object.
     * @return A non-null reference to a TemplatesHandler, that may be used as a
     *    ContentHandler for SAX parse events.
     *
     * @throws TransformerConfigurationException - If for some reason the 
     * TemplatesHandler cannot be created.    
     */
    public TemplatesHandler newTemplatesHandler()
        throws TransformerConfigurationException
    {
        try {
            return new TemplatesHandlerImpl(this, new XMLProcessorImpl());
        } catch(Exception e) {
            throw toConfigException(e);
        }
    }
    
    /**
     *
     */    
    public TransformerHandler newTransformerHandler()
        throws TransformerConfigurationException
    {
        // FIXME: implement
        throw new TransformerConfigurationException("newTransformerHandler() for null transform not yet implemented");
    }
    
    /**
     *
     */
    public TransformerHandler newTransformerHandler(Source source)
        throws TransformerConfigurationException
    {
        return newTransformerHandler(newTemplates(source));
    }
    
    /**
     *
     */
    public TransformerHandler newTransformerHandler(Templates templates)
        throws TransformerConfigurationException
    {

        try {
            return new TransformerHandlerImpl((TransformerImpl)templates.newTransformer());
        } catch(Exception e) {
            throw toConfigException(e);
        }
    }

    /**
     * 
     */
    public XMLFilter newXMLFilter(Source source)
        throws TransformerConfigurationException
    {
        return newXMLFilter(newTemplates(source));
    }

    /**
     * 
     */
    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException
    {

        try {
            return new TransformerFilterImpl((TransformerImpl)templates.newTransformer());
        } catch(Exception e) {
            throw toConfigException(e);
        }

    }

    //
    //
    //
    private TransformerConfigurationException toConfigException(Exception e)
    {
        if (e instanceof TransformerConfigurationException) {
            return (TransformerConfigurationException) e;
        } else {
            return new TransformerConfigurationException(e);
        }
    }
    
}


