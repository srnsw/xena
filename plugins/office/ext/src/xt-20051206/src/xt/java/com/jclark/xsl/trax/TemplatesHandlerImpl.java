// $Id$

package com.jclark.xsl.trax;

import com.jclark.xsl.sax2.SAXTwoOMBuilder;
import com.jclark.xsl.sax2.XMLProcessorImpl;

import com.jclark.xsl.sax.ExtensionHandlerImpl;

import com.jclark.xsl.tr.XMLProcessor;
import com.jclark.xsl.tr.Sheet;
import com.jclark.xsl.tr.Engine;
import com.jclark.xsl.tr.EngineImpl;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.SAXException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TemplatesHandler;

import java.io.IOException;

/**
 * An implementation of <code>javax.xml.transform.sax.TemplatesHandler</code>.
 * Collects SAX events representing an XSLT stylesheet, and provides
 * access to an object representing the "compiled" sheet
 */
class TemplatesHandlerImpl extends XMLFilterImpl
    implements TemplatesHandler
{

    private TransformerFactoryImpl _factory;
    private String _systemId = null;

    private Templates _templates = null;

    private SAXTwoOMBuilder _builder = null;
    private XMLProcessor _sheetModelBuilder = null;
    private Engine _engine = null;

    /**
     * construct with a reference to the factory, and a processor
     * which can construct an object model from the SAX events
     * representing the stylesheet
     */
    protected TemplatesHandlerImpl(TransformerFactoryImpl factory,
                                   XMLProcessor sheetModelBuilder)
        throws SAXException
    {
        super();
        _factory = factory;
        _sheetModelBuilder = sheetModelBuilder;
        _engine = new EngineImpl(_sheetModelBuilder,
                                 new ExtensionHandlerImpl());
    }
    
    /**
     *  When a TemplatesHandler object is used as a ContentHandler for
     *  the parsing of transformation instructions, it creates a Templates
     *   object, which the caller can get once the SAX events have been
     *   completed.
     */
    public Templates getTemplates()
    { 
        try {
            Sheet sheet = _engine.createSheet(_builder.getRootNode());
            if (sheet == null ) {
                throw new Exception("unable to build xheet");
            }
            _templates = new TemplatesImpl(sheet, _engine, _factory);
        } catch (Exception ex) {
            // FIXME: just here for debugging
            System.err.println("trouble creating sheet");
            ex.printStackTrace();
            return null;
        }
        return _templates;
    }

    /**
     * SAX implementation: recieve notification of start of parse,
     *  redirect all subsequent SAX events to our embedded object model
     *  builder
     */    
    public void startDocument() throws SAXException
    {
        // FIXME: what should we do if no SystemId has been set?

        //        NamespaceDeclAugmenter filter = new NamespaceDeclAugmenter();
        _builder = XMLProcessorImpl.createBuilder(_systemId,
                                                  0,
                                                  _engine.getSheetLoadContext(),
                                                  _engine.getNameTable());
        
        // filter.setParent(this);
        super.setContentHandler(_builder);
        // filter.setContentHandler(_builder);
        // error handler?
        super.setDTDHandler(_builder);
        //        filter.setDTDHandler(_builder);
        
        _builder.startDocument();
    }

    /**
     * end of stylesheet SAX events, compile it
     */
    public void endDocument() throws SAXException
    {
        _builder.endDocument();
    }

    /**
     * sets the base URI we want to associate with the stylesheet
     */
    public void setSystemId(String systemId)
    {
        _systemId = systemId;
    }
    
    /**
     * qets the base URI associated with the stylesheet
     */    
    public String getSystemId()
    {
        return _systemId;
    }

}


