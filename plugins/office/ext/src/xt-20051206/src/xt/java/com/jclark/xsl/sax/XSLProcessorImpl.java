// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;

import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;
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
    private XMLProcessorEx sourceLoader;
    private XMLProcessorEx sheetLoader;
    private boolean engineMatchesSheetLoader = false;
    private Parser sheetParser;
    private Parser sourceParser;
    private Sheet sheet;
    private Engine engine;
    private InputSource sheetSource;

    private Result result;

    private OutputMethodHandler outputMethodHandler;
    private DocumentHandler documentHandler;
    private ContentHandler _contentHandler = null;

    private ErrorHandler errorHandler;
    private Hashtable params = new Hashtable();

    private SaxFilterMaker _xrap = null;
    private ActionDebugTarget _debugger = null;

    /**
     *
     */
    public void setParser(Parser sourceParser, Parser sheetParser)
    {

        this.sourceParser = sourceParser;
        this.sheetParser = sheetParser;

        sourceLoader = new XMLProcessorImpl(sourceParser);
        if (sourceParser == sheetParser) {
            sheetLoader = sourceLoader;
        } else {
            sheetLoader = new XMLProcessorImpl(sheetParser);
        }
        engineMatchesSheetLoader = false; // haven't compiled the XSLT, yet
    }

    // who uses this?
    public void setParser(XMLProcessorEx loader)
    {
        this.sourceParser = null;
        this.sheetParser = null;
        sourceLoader = loader;
        sheetLoader = loader;
        engineMatchesSheetLoader = false;
    }

    /**
     * use same parser for input and styleSheet
     */
    public void setParser(Parser parser)
    {
        setParser(parser, parser);
    }

    /**
     * SAX 1 Parser API
     */
    public void setLocale(Locale locale) throws SAXException
    {
        if (sheetParser != null) {
            sheetParser.setLocale(locale);
        }
        if (sourceParser != null) {
            sourceParser.setLocale(locale);
        }
    }

    /**
     *  SAX 1 Parser API
     */
    public void setDTDHandler(DTDHandler handler)
    { }

    /**
     * SAX 1 PArser API
     */
    public void setEntityResolver(EntityResolver resolver)
    {
        if (sheetParser != null) {
            sheetParser.setEntityResolver(resolver);
        }
        if (sourceParser != null) {
            sourceParser.setEntityResolver(resolver);
        }
    }

    /**
     * we can have either an OutputMethodHandler,
     *   or a DocumentHandler, not both
     */
    public void setOutputMethodHandler(OutputMethodHandler handler)
    {
        outputMethodHandler = handler;
        _contentHandler = null;
        documentHandler = null;
    }

    /**
     * we can have either an OutputMethodHandler, 
     *  or a DocumentHandler, not both
     */
    public void setDocumentHandler(DocumentHandler handler)
    {
        documentHandler = handler;
        _contentHandler = null;
        outputMethodHandler = null;
    }

    /**
     * we can have either an OutputMethodHandler, 
     *   a ContentHandler or DocumentHandler, not two or three
     */
    public void setContentHandler(ContentHandler handler)
    {
        _contentHandler = handler;
        outputMethodHandler = null;
        documentHandler = null;
    }

    /**
     * run the transform
     */
    public void parse(String systemId) throws SAXException, IOException
    {
        parse(new InputSource(systemId));
    }

    /**
     * SAX 1 PArser API
     */
    public void setErrorHandler(ErrorHandler handler)
    {
        if (sheetParser != null) {
            sheetParser.setErrorHandler(handler);
        }
        if (sourceParser != null) {
            sourceParser.setErrorHandler(handler);
        }
        if (sheetLoader != null) {
            sheetLoader.setErrorHandler(handler);
        }
        if (sourceLoader != null) {
            sourceLoader.setErrorHandler(handler);
        }
        this.errorHandler = handler;
    }
    
    /**
     * load (and compile) the stylesheet
     */
    public void loadStylesheet(InputSource sheetSource) 
        throws SAXException, IOException
    {
        if (!engineMatchesSheetLoader) {
            if (sheetLoader == null) {
                throw new Error("loadStylesheet called before setParser");
            }
            engine = new EngineImpl(sheetLoader,
                                    new ExtensionHandlerImpl());
            engineMatchesSheetLoader = true;
        }
        try {
            // build a DOM
            phase(1);
            Node node = sheetLoader.load(sheetSource,
                                         0,
                                         engine.getSheetLoadContext(),
                                         engine.getNameTable());
            // "compile" it
            phase(2);
            if (_debugger != null) {
                sheet = engine.createSheet(node, _debugger);
            } else {
                sheet = engine.createSheet(node);
            }
        }
        catch (XSLException e) {
            // TEMP
            // e.printStackTrace();

            handleXSLException(e);
        }
    }

    /**
     * load the input document into a (xslt) object model,
     *   and run the transform
     */ 
    public void parse(InputSource source) throws SAXException, IOException
    {
        try {
            if (outputMethodHandler != null) {
                result = new MultiNamespaceResult(outputMethodHandler, errorHandler);
            } else if (_contentHandler != null) {
                result = 
                    new com.jclark.xsl.sax2.MultiNamespaceResult(_contentHandler, 
                                                                 errorHandler);
            } else if (documentHandler != null) {
                result = new MultiNamespaceResult(documentHandler, errorHandler);
            } else {
                result = new MultiNamespaceResult(new HandlerBase(), errorHandler);
            }
            
            // build a DOM of the input
            phase(3);
            Node root = sourceLoader.load(source,
                                          0,
                                          sheet.getSourceLoadContext(),
                                          engine.getNameTable());

            // run the transform
            phase(4);

            if (_xrap != null) {
                sheet.setSaxExtensionFilter(_xrap);
            }

            sheet.process(root, sourceLoader, this, result);

            // that's all folks
            phase(5);
        }
        catch (XSLException e) {
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
     * sets a debugging target
     */
    public void setDebugger(String name, ActionDebugTarget debugger)
    {
        // FIXME: do this properly
        _debugger = debugger; 
    }

    /**
     *
     */
    void handleXSLException(XSLException e) throws SAXException, IOException
    {
        String systemId = null;
        int lineNumber = -1;
        Node node = e.getNode();
        if (node != null) {
            URL url = node.getURL();
            if (url != null) {
                systemId = url.toString();
            }
            lineNumber = node.getLineNumber();
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
    { /* System.out.println("XSLProcessorImpl():: phase " + n); */ }

    public Object clone()
    {
        try {
            XSLProcessorImpl cloned = (XSLProcessorImpl)super.clone();
            cloned.params = new Hashtable(); // (Hashtable) cloned.params.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            throw new Error("unexpected CloneNotSupportedException");
        }
    }

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
        return params.get(nameString);
    }

    public void setParameter(String name, Object obj)
    {
        params.put(name, obj);
    }
}

