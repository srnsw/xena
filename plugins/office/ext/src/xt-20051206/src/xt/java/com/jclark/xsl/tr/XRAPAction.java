// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

import com.jclark.xsl.expr.StringExpr;

import com.jclark.xsl.sax.MultiNamespaceResult;
import com.jclark.xsl.sax.SaxFilterMaker;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

/**
 * Invokes an XRAP processor
 */
class XRAPAction implements Action
{

    private Action _content;
    private NamespacePrefixMap _nsMap;

    XRAPAction(NamespacePrefixMap nsMap, Action content)
    {
        _nsMap = nsMap;
        _content = content;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, 
                       Node sourceNode,
                       Result result) throws XSLException
    {
        
        // this will be the input XMLReader for the XRAP processor's input
        // we evaluate the contents (Actions) of this template,
        // which presumably are XRAP commands, and make them
        // available to the XRAP processor through this XMLReader
        ResultReaderAdapter xrapInputReader = 
            new ResultReaderAdapter(context, _content, sourceNode);

        SaxFilterMaker fm = context.getSaxExtensionFilter();
        if (fm == null) {
            throw new XSLException("XRAPAction::no SaxFilterMaker");
        }

        // filt encapsulates the XRAP processor
        XMLFilter filt = fm.getFilter();
        filt.setParent(xrapInputReader);

        // the filter (XRAP processor) writes its results to
        // a SAX ContentHandler.  Our implementation of 
        // ContentHandler transforms the SAX events into
        // method calls on an xt Result object
        ContentHandlingResultWriter chrw = 
            new ContentHandlingResultWriter(_nsMap.getNameTable().getEmptyNamespacePrefixMap(),
                                            filt, result);

        filt.setContentHandler(chrw);

        try {
            // FIXME: any way to set the base URI here?
            filt.parse("dummy");
        } catch (Exception ex) {
            throw new XSLException(ex);
        }

    }

    /**
     * causes an xt Result to masquerade as an XMLReader
     * used as a place for the XSLT content (Actions) to be
     * written to and then interpreted as XRAP
     */
    private class ResultReaderAdapter extends XMLFilterImpl
    {

        Action _content;
        ProcessContext _context;
        Node _sourceNode;

        Result _xrapEventProcessor; // where we'll write our nested contents

        ParserAdapter _parserAdapter;
        private ContentHandler _handler;

        ResultReaderAdapter(ProcessContext context,
                            Action content, Node sourceNode)
        {
            _context = context;
            _content = content;
            _sourceNode = sourceNode;
        }

        /**
         * The XRAP processor says it want's the XRAP sent to itself
         */
        public void setContentHandler(ContentHandler xrapProcessor)
        {

            try {
                _parserAdapter = new ParserAdapter();
            } catch (SAXException ex) {
                // whoops!
            }

            _parserAdapter.setContentHandler(xrapProcessor);

            _xrapEventProcessor = 
                new MultiNamespaceResult(_parserAdapter, null);
            
        }
        
        public void parse(String sysid) throws SAXException
        {
            process();
        }

        public void parse(InputSource src) throws SAXException
        {
            process();
        }

        private void process() throws SAXException
        {
            try {
                super.startDocument();
                // fire our contents to the XRAP processor's ContentHandler
                _content.invoke(_context, _sourceNode, _xrapEventProcessor);

                super.endDocument();

            } catch (XSLException ex) {
                throw new SAXException(ex);
            }
        }
    }
}
