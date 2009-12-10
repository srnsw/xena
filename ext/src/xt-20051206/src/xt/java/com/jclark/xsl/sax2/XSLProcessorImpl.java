// $Id$

package com.jclark.xsl.sax2;

import com.jclark.xsl.sax.ExtensionHandlerImpl;
import com.jclark.xsl.sax.SaxFilterMaker;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;

import javax.xml.transform.SourceLocator;

import java.util.Locale;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

/**
 * An XSLT Processor 
 */
public class XSLProcessorImpl implements XSLProcessor, Cloneable,
                                         ParameterSet
{

    private XMLProcessorEx _sourceModelBuilder;
    private XMLProcessorEx _sheetModelBuilder;

    private boolean _engineMatchesSheetLoader = false;

    private XMLReader _sheetReader;
    private XMLReader _sourceReader;

    private Sheet _sheet;
    private Engine _engine;

    private InputSource _sheetSource;

    private ResultBase _result;
    private OutputMethodHandler _outputMethodHandler;

    private ContentHandler _contentHandler;

    private DTDHandler _dtdHandler;
    private EntityResolver _resolver;
    private ErrorHandler _errorHandler;
    private Hashtable _params = new Hashtable();

    private SaxFilterMaker _xrap = null;
    private ActionDebugTarget _debugger = null;

    private Node _sourceRoot = null;


    public XSLProcessorImpl()
    {}

    public XSLProcessorImpl(Sheet sheet, Engine engine)
    {
        _sheet = sheet;
        _engine = engine;
        _engineMatchesSheetLoader = true;
    }

    /**
     * set two XMLReaders (may be the same XMLReader), #1 for
     *  parsing the XML source to transform, #2 for parsing the stylesheeet.
     */
    public void setReaders(XMLReader sourceReader, XMLReader sheetReader)
    {

        _sourceReader = sourceReader;
        _sheetReader = sheetReader;

        _sourceModelBuilder = new XMLProcessorImpl(sourceReader);
        if (_sourceReader == _sheetReader) {
            _sheetModelBuilder = _sourceModelBuilder;
        } else {
            _sheetModelBuilder = new XMLProcessorImpl(_sheetReader);
        }
        _engineMatchesSheetLoader = false; // haven't compiled the XSLT, yet
    }

    /**
     * prepare for parsing the input XML document
     */
    public void setSourceReader(XMLReader sourceReader)
    {
        _sourceReader = sourceReader;
        _sourceModelBuilder = new XMLProcessorImpl(sourceReader);
    }

    /**
     *  SAX XMLReader API
     */
    public void setDTDHandler(DTDHandler handler)
    { 
        _dtdHandler = handler; 
    }

    /**
     *  SAX XMLReader API
     */
    public DTDHandler getDTDHandler()
    { 
        return _dtdHandler; 
    }

    /**
     * SAX XMLReader API
     */
    public void setEntityResolver(EntityResolver resolver)
    {
        if (_sheetReader != null) {
            _sheetReader.setEntityResolver(resolver);
        }
        if (_sourceReader != null) {
            _sourceReader.setEntityResolver(resolver);
        }
    }

    /**
     * SAX XMLReader API
     */
    public EntityResolver getEntityResolver()
    { return _resolver; }

    /**
     * we can have either an OutputMethodHandler,
     *   or a DocumentHandler, not both
     */
    public void setOutputMethodHandler(OutputMethodHandler handler)
    {
        _outputMethodHandler = handler;
        _contentHandler = null;
    }

    /**
     * SAX 2 XMLReader API. .. return the content handler this is writing to
     * @return null in no ContentHandler was set
     */
    public ContentHandler getContentHandler()
    { return _contentHandler; }

    /**
     * SAX 2 XMLReader API.
     * we can have either an OutputMethodHandler, 
     *  or a ContentHandler, not both
     */
    public void setContentHandler(ContentHandler handler)
    {
        _contentHandler = handler;
        _outputMethodHandler = null;
    }

    // FIXME
    public ContentHandler getSourceBuilder()
    {
        return null;

    }


    /**
     * SAX API
     */
    public void setErrorHandler(ErrorHandler handler)
    {
        if (_sheetReader != null) {
            _sheetReader.setErrorHandler(handler);
        }
        if (_sourceReader != null) {
            _sourceReader.setErrorHandler(handler);
        }
        if (_sheetModelBuilder != null) {
            _sheetModelBuilder.setErrorHandler(handler);
        }
        if (_sourceModelBuilder != null) {
            _sourceModelBuilder.setErrorHandler(handler);
        }
        _errorHandler = handler;
    }

    /**
     * SAX API 
     * @return the error handler if one has been set, else <code>null</code>.
     */
    public ErrorHandler getErrorHandler()
    { return _errorHandler; }

    /**
     * SAX API
     */
    public void setFeature(String featureURI, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        // FIXME: we can do better
        throw new SAXNotSupportedException("XSLProcessor cannot set feature: " +
                                           featureURI);
    }

    /**
     * SAX API
     */
    public boolean getFeature(String featureURI)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        // FIXME: we can do better
        throw new SAXNotSupportedException("XSLProcessor cannot get feature: " +
                                           featureURI);
    }

    /**
     * SAX API
     */
    public Object getProperty(java.lang.String name)
        throws SAXNotRecognizedException,
               SAXNotSupportedException
               
    {
        // FIXME: we can do better
        throw new SAXNotSupportedException("XSLProcessor cannot get property: " +
                                           name);
    }

    /**
     * SAX API
     */
    public void setProperty(java.lang.String name,
                            java.lang.Object value)
        throws SAXNotRecognizedException,
               SAXNotSupportedException
    {
        // FIXME: we can do better
        throw new SAXNotSupportedException("XSLProcessor cannot set property: " +
                                           name);
    }
    ////////////////////////////////////////////////////


    /**
     * load (and compile) the stylesheet
     */
    public void loadStylesheet(InputSource sheetSource) 
        throws SAXException, IOException
    {
        if (!_engineMatchesSheetLoader) {
            if (_sheetModelBuilder == null) {
                throw new Error("loadStylesheet called before setParsers");
            }
            _engine = new EngineImpl(_sheetModelBuilder,
                                     new ExtensionHandlerImpl());
            _engineMatchesSheetLoader = true;
        }
        try {
            // build an object model
            phase(1);
            Node node = _sheetModelBuilder.load(sheetSource,
                                                0,
                                                _engine.getSheetLoadContext(),
                                                _engine.getNameTable());
            // "compile" it
            phase(2);
            _sheet = _engine.createSheet(node);
        }
        catch (XSLException e) {
            // TEMP
            // e.printStackTrace();

            handleXSLException(e);
        }
    }

    /**
     * sets an extension element processor for XRAP
     */
    public void setSaxExtensionFilter(String name, SaxFilterMaker xrap)
    {
        // FIXME: do this properly
        _xrap = xrap; 
    }

    /**
     * sets an extension element processor for XRAP
     */
    public void setDebugger(String name, ActionDebugTarget debugger)
    {
        // FIXME: do this properly
        _debugger = debugger; 
    }


    /**
     * SAX XMLReader API
     */
    public void parse(String sourceURI) throws SAXException, IOException
    {
        parse(new InputSource(sourceURI));
    }

    /**
     * load the input document into a (xslt) object model,
     *   and run the transform
     */ 
    public void parse(InputSource source) throws SAXException, IOException
    {
        try {
            configureResult();

            if (_sheet == null) {
                throw new SAXException("no compiled stylesheet!");
            }
            if (_engine == null) {
                throw new SAXException("no engine");
            }
            if (_sourceModelBuilder ==  null) {
                throw new SAXException("no object model builder");
            }
            
            // build a DOM of the input
            phase(3);
            Node sourceRoot = _sourceModelBuilder.load(source,
                                                   0,
                                                   _sheet.getSourceLoadContext(),
                                                   _engine.getNameTable());
            
            phase(4);
            transform(sourceRoot);
            // that's all folks
            phase(5);
        }
        catch (XSLException e) {
            handleXSLException(e);
        }
    }

    public void configureResult() {
        
        if (_outputMethodHandler != null) {
            _result = new MultiNamespaceResult(_outputMethodHandler,
                                               _errorHandler);
        } else if (_contentHandler != null) {
            _result = new MultiNamespaceResult(_contentHandler, 
                                               _errorHandler);
        } else {
            // should never happen when we're using TrAX api
            _result = new MultiNamespaceResult(new DefaultHandler(), 
                                               _errorHandler);
        }
    }

    public void transform(Node sourceRoot)  throws XSLException
    {
        
        if (_xrap != null) {
            _sheet.setSaxExtensionFilter(_xrap);
        }
        if (_debugger != null) {
            _sheet.setDebugger(_debugger);
        }
        
        // run the transform
        _sheet.process(sourceRoot, _sourceModelBuilder, this, _result);
        
    }


    public SAXTwoOMBuilder getSourceOMBuilder(String systemId)
    {
        if (_sourceModelBuilder == null) {
            return null;
        } 

        return _sourceModelBuilder.getConfiguredOMBuilder(systemId, 
                                                          0,
                                                          _sheet.getSourceLoadContext(),
                                                          _engine.getNameTable()
                                                          );
    }


    /**
     * rethrows an XSLException as a SAXException
     */
    void handleXSLException(XSLException e) throws SAXException, IOException
    {
        // FIXME: maybe we want to send it to the TRaX calling app, instead
        // por at least the error handler?

        String systemId = null;
        int lineNumber = -1;

        SourceLocator sl  = e.getLocator();
        if (sl != null) {
            systemId = sl.getSystemId();
            lineNumber = sl.getLineNumber();
        }

        Exception wrapped = (Exception) e.getException();
        String message = e.getMessage();
        if (systemId != null || lineNumber != -1) {
            throw new SAXParseException(message, null, 
                                        systemId, lineNumber, -1, wrapped);
        }
        if (message == null) {
            if (wrapped instanceof SAXException) {
                throw (SAXException)wrapped;
            }
            if (wrapped instanceof IOException) {
                throw (IOException)wrapped;
            }
        }
        throw new SAXException(message, wrapped);
    }

    void phase(int n)
    {  /* System.out.println("XSLProcessorImpl():: phase " + n); */ }

    /**
     *
     */
    public Object clone()
    {
        try {
            XSLProcessorImpl cloned = (XSLProcessorImpl)super.clone();
            cloned._params = (Hashtable) cloned._params.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            throw new Error("unexpected CloneNotSupportedException");
        }
    }

    ///////////////////////////////////////////////////////////
    //
    //  XSLT Parameters

    /**
     *
     */
    public Object getParameter(Name name)
    {
        String nameString = name.getNamespace();
        if (nameString == null) {
            nameString = name.getLocalPart();
        } else {
            nameString = (nameString
                          + OutputMethodHandler.namespaceSeparator
                          + name.getLocalPart());
        }
        return _params.get(nameString);
    }

    /**
     * recognizes names in the form "{namespace-part}local-part"
     * as used in TrAX
     */
    public void setParameter(String name, Object obj)
    {
        if (name.length() > 1 && name.charAt(0) == '{') {
            int nsend = name.indexOf('}');
            if (nsend > 0) {
                name = name.substring(1, nsend) +
                           OutputMethodHandler.namespaceSeparator
                    + name.substring(nsend + 1);

            }
        }
        _params.put(name, obj);
    }

    /**
     * recognizes names in the form "{namespace-part}local-part"
     * as used in TrAX
     */
    public Object getParameter(String name)
    {
        if (name.length() > 1 && name.charAt(0) == '{') {
            int nsend = name.indexOf('}');
            if (nsend > 0) {
                name = name.substring(1, nsend) +
                           OutputMethodHandler.namespaceSeparator
                    + name.substring(nsend + 1);

            }
        }
        return _params.get(name);
    }

    /**
     *
     */
    public void clearParameters()
    {
        _params = new Hashtable();
    }

}

