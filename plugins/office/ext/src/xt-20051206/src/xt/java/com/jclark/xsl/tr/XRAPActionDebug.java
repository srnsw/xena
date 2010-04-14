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
class XRAPActionDebug extends XRAPAction
{

    private ActionDebugTarget _target;
    private String _templateIDHook;
    private Node _sheetNode;

    /**
     *
     */
    XRAPActionDebug(                             
                    ActionDebugTarget target, 
                    Node sheetNode,
                    String templateIDHook,
                    NamespacePrefixMap nsMap, 
                    Action content)
    {
        super(nsMap, content);
        _target = target;
        _sheetNode = sheetNode;
        _templateIDHook = templateIDHook;

    }

    /**
     *
     */
    public void invoke(ProcessContext context, 
                       Node sourceNode,
                       Result result) throws XSLException
    {
        super.invoke(context, sourceNode, result);
    }

}
