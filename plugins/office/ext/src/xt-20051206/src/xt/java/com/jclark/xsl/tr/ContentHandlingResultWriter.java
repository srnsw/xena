// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

import com.jclark.xsl.sax.MultiNamespaceResult;

/**
 * Converts a SAX 2 event stream to calls on a Result -- which
 * is useful for connecting to extension elements
 * which generate SAX events
 */
class ContentHandlingResultWriter extends XMLFilterImpl
{

    private Result _downstream = null;
    private NamespacePrefixMap _nsMap;

    private Name _names[] = new Name[100];
    private int _stack = 0;

    ContentHandlingResultWriter(NamespacePrefixMap nsMap,
                                XMLReader upstream,
                                Result downstream)
    {
        super(upstream);
        _nsMap = nsMap;
        _downstream = downstream;
    }


    public void characters(char[] ch, int start, int length) 
        throws SAXException
    {
        try {
            _downstream.characters(new String(ch, start, length));
        } catch (XSLException ex) {
            throw new SAXException(ex);
        }
    }

    public void startElement(String nsURI, 
                             String localName, 
                             String qname, 
                             Attributes atts)
        throws SAXException
    {
        try {
            Name name;

            int i = qname.indexOf(':');
            if (nsURI == null || nsURI.length() == 0) {
                name = _nsMap.getNameTable().createName(qname.substring(i + 1));
            } else {
                if (i > 0) {
                    _nsMap = _nsMap.bind(qname.substring(0, i), nsURI);
                } else {
                    _nsMap = _nsMap.bindDefault(nsURI);
                }
                name = _nsMap.expandElementTypeName(qname, null);
            }

            _downstream.startElement(name, _nsMap);

            _names[_stack++] = name;

            int natts = atts.getLength();
            for (int j = 0; j < natts; ++j) {
                writeAttribute(atts.getURI(j), atts.getQName(j),
                               atts.getValue(j));
            }
            
        } catch (XSLException ex) {
            throw new SAXException(ex);
        }
    }

    public void endElement(String namespaceURI, 
                           String localName, 
                           String qName) 
        throws SAXException
    {

        try {
            _downstream.endElement(_names[--_stack]);
        } catch (XSLException ex) {
            throw new SAXException(ex);
        }

    } 

    private void writeAttribute(String ns, String qname, String value)
        throws XSLException
    {

        Name name;

        if (ns.length() == 0) {
            name = 
                _nsMap.getNameTable().createName(qname.substring(qname.indexOf(';') +
                                                                 1));
        } else {
            name = _nsMap.getNameTable().createName(qname, ns);
        }

        _downstream.attribute(name, value);
    }
}
