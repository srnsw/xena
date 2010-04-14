//

package com.jclark.xsl.trax;

import java.io.StringReader;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;

import java.util.Enumeration;
import java.util.Hashtable;

import com.jclark.xsl.sax2.XMLProcessorImpl;

import org.xml.sax.InputSource;

import org.xml.sax.helpers.XMLFilterImpl;
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
 *
 */
public class TransformerFilterImpl extends TransformerHandlerImpl
{

    /**
     *
     */
    public TransformerFilterImpl(TransformerImpl transformer) throws SAXException
    {
        super(transformer);
    }

    /**
     *
     */
    public void setContentHandler(ContentHandler ch)
    {
        super.setResult(new SAXResult(ch));
    }

}
